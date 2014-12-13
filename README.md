asyncop
=======

A very helpful framework for asynchronous operation related to an Activity or Fragment for Android application


How to use
==========

With Android Studio add dependency:

	compile 'com.github.mguidi.asyncop:asyncop:1.0.0'


Define your async operation extending the AsyncOp Class:

	public class LongOp extends AsyncOp {

    	@Override
	    public Bundle execute(Context context, Bundle args) {
        
    	    Bundle result = new Bundle();
        
        	// define your operation here

	        return result;
    	}

	}
	
Map your operation with an action in your Application class:

	public class Application extends android.app.Application {

    	@Override
	    public void onCreate() {
    	    super.onCreate();

        	AsyncOpManager.getInstance(this).mapOp("long_op", LongOp.class);
    	}
	}
	
Execute the operation inside an Activity or a Fragment

	public class MyActivity extends ActionBarActivity implements AsyncOpCallback, View.OnClickListener {

	    private AsyncOpHelper mOpHelper;

    	@Override
	    protected void onCreate(Bundle savedInstanceState) {
    	    super.onCreate(savedInstanceState);
        	setContentView(R.layout.activity_my);

			// initialize the Async op helper on the onCreate
	        mOpHelper = new AsyncOpHelper(this, savedInstanceState, this);

    	    findViewById(R.id.btnHello).setOnClickListener(this);
    	}

	    @Override
    	protected void onResume() {
        	super.onResume();
	        mOpHelper.onResume();
    	}

	    @Override
    	protected void onSaveInstanceState(Bundle outState) {
        	super.onSaveInstanceState(outState);
	        mOpHelper.onSaveInstanceState(outState);
    	}

	    @Override
    	protected void onPause() {
        	super.onPause();
	        mOpHelper.onPause();
    	}

	    @Override
		public void onAsyncOpFinish(int idRequest, String action, Bundle args, Bundle result) {
        	if (action.equals("long_op")) {

				// handle the result here

        	}
	    }

    	@Override
	    public void onAsyncOpFail(int idRequest, String action) {
        	if (action.equals("long_op")) {
            	
            	// hadle the operaion fail here
            	// the onAscynOpFail is called only in the case that your async op was running when the system killed it
            	
	        }
    	}

	    @Override
    	public void onClick(View v) {
        	if (v.getId() == R.id.btnHello) {

				// define a bundle with the async op params
            	Bundle args = new Bundle();
            	
				// execute the async op mapped with the action
				mOpHelper.execute("long_op", args);
        	}
    	}
	}
