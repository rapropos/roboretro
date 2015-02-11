package com.robovm.roboretro;

import org.robovm.apple.foundation.Foundation;
import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.uikit.UIApplication;
import org.robovm.apple.uikit.UIApplicationDelegateAdapter;
import org.robovm.apple.uikit.UIApplicationLaunchOptions;
import retrofit.RestAdapter;
import retrofit.client.Response;
import retrofit.mime.TypedInput;
import rx.Observable;

public class RoboRetroApp extends UIApplicationDelegateAdapter {
    private Mothership mothership;

    @Override
    public boolean didFinishLaunching (UIApplication app,
                                       UIApplicationLaunchOptions launchOptions) {
        app.addStrongRef( this );

        mothership = new RestAdapter.Builder()
                .setEndpoint( "http://www.robovm.com" )
                .setLogLevel( RestAdapter.LogLevel.FULL )
                .build()
                .create( Mothership.class );

        // because of the concurrency, register1 will likely not have
        // enough time to complete before register2 crashes. if you
        // wish to see register1 in action, comment out the call to
        // register2.
        register1();
        register2();

        return true;
    }

    // These registration functions should be functionally identical.
    // The application this test case is abstracted from requires
    // the second version, because it is actually combining multiple
    // Observables coming from different asynchronous sources in order
    // to provide a registration record which is then fed to an
    // upstream web service and the response is fed as another
    // Observable to multiple subscribers.

    // This version which has only a single producer should work
    private void register1() {
        mothership.register( "" )
                .subscribe( ( rsp ) -> {
                    logit( summarize( rsp ) );
                } );
    }

    // This version which has multiple producers and consumers
    // should crash an auxiliary thread with the main thread doing
    // an OSAtomicCompareAndSwap64 via an Unsafe.putLongVolatile
    // triggered from an rxjava internal ObjectPool initializer.
    private void register2() {
        Observable.just( "" )
                .flatMap( mothership::register )
                .map( this::summarize )
                .subscribe( this::logit );
    }

    private String summarize( Response rsp ) {
        StringBuilder sb = new StringBuilder();
        sb.append( rsp.getStatus() );
        TypedInput body = rsp.getBody();
        if( null != body ) {
            sb.append( ": " );
            sb.append( body.length() );
        }
        String rv = sb.toString();
        return rv;
    }

    private void logit( String s ) {
        Foundation.log( s );
    }

    public static void main (String[] args) {
        NSAutoreleasePool rsrc_pool = null;
        try {
            rsrc_pool = new NSAutoreleasePool();
            UIApplication.main( args, null, RoboRetroApp.class );
        } finally {
            if( null != rsrc_pool ) {
                rsrc_pool.close();
            }
        }
    }
}
