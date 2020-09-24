package com.maxsavteam.newmcalc2.core;

import android.content.Context;
import android.util.Log;

import java.math.BigDecimal;

public class CoreSubProcess {

	private BigDecimal mResult = null;
	private final Context mContext;
	private final String TAG = "CoreSubProcess";

	public CalculationError getError() {
		return error;
	}

	public boolean isWasError() {
		return mWasError;
	}

	private CalculationError error;

	private boolean mWasError = false;

	public BigDecimal getResult() {
		return mResult;
	}

	public void run(String ex) {
		CalculationCore calculationCore = new CalculationCore( mContext, new CalculationCore.CoreInterface() {
			@Override
			public void onSuccess(CalculationCore.CalculationResult calculationResult) {
				mResult = calculationResult.getResult();
			}

			@Override
			public void onError(CalculationError calculationError) {
				mWasError = true;
				CoreSubProcess.this.error = calculationError;
				mResult = null;
				if ( error.getStatus().equals( "Core" ) ) {
					if ( error.getErrorMessage().contains( "String is number" ) ) {
						mResult = error.getPossibleResult();
					}
				}
			}
		} );

		Log.i( TAG, "run with ex=" + ex );
		calculationCore.prepareAndRun( ex, "isolated" );
	}

	CoreSubProcess(Context context) {
		this.mContext = context;
		Log.i( TAG, "constructor" );
	}

}
