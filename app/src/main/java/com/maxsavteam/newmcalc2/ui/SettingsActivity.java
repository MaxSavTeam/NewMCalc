package com.maxsavteam.newmcalc2.ui;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.maxsavteam.newmcalc2.BuildConfig;
import com.maxsavteam.newmcalc2.Main2Activity;
import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.ThemeActivity;
import com.maxsavteam.newmcalc2.types.Tuple;
import com.maxsavteam.newmcalc2.widget.ButtonWithDropdown;
import com.maxsavteam.newmcalc2.widget.CustomAlertDialogBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SettingsActivity extends ThemeActivity {

	private SharedPreferences sp;
	private final String mAppType = BuildConfig.APPTYPE;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		//Toast.makeText(getApplicationContext(), Integer.toString(id) + " " + Integer.toString(R.id.home), Toast.LENGTH_SHORT).show();
		if ( id == android.R.id.home ) {
			onBackPressed();
		}
		return super.onOptionsItemSelected( item );
	}

	public void switchSave(View v) {
		Switch sw = (Switch) v;
		if ( v.getId() == R.id.switchSaveOnExit ) {
			if ( sw.isChecked() ) {
				sw.setText( R.string.switchSaveOn );
				sp.edit().putBoolean( "saveResult", true ).apply();
			} else {
				sw.setText( R.string.switchSaveOff );
				sp.edit().remove( "saveResult" ).apply();
			}
		}

	}

	@SuppressLint("SourceLockedOrientationActivity")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		sp = PreferenceManager.getDefaultSharedPreferences( getApplicationContext() );
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_settings );

		setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );

		Toolbar toolbar = findViewById( R.id.toolbar );
		setSupportActionBar( toolbar );
		getSupportActionBar().setDisplayHomeAsUpEnabled( true );

		if ( sp.getBoolean( "storage_denied", false ) ) {
			findViewById( R.id.import_export ).setVisibility( View.GONE );
		}

		ButtonWithDropdown button = findViewById( R.id.theme_dropdown_button );
		button.setElements( getResources().getStringArray( R.array.theme_states ) );
		button.setSelection( sp.getInt( "theme_state", 2 ) );
		button.setOnItemSelectedListener( index->{
			sp.edit().putInt( "theme_state", index ).apply();
			restartApp();
		} );

		findViewById( R.id.btn_show_guide ).setOnClickListener( view->{
			sp.edit().remove( "guide_first_start" )
					.remove( "history_guide" )
					.apply();
			Toast.makeText( this, R.string.guide_will_be_shown_next_time, Toast.LENGTH_SHORT ).show();
		} );
	}

	@Override
	protected void onPostCreate(@Nullable Bundle savedInstanceState) {
		super.onPostCreate( savedInstanceState );
		Switch sw = findViewById( R.id.switchSaveOnExit );
		sw.setChecked( sp.getBoolean( "saveResult", false ) );
		if ( sw.isChecked() ) {
			sw.setText( R.string.switchSaveOn );
		} else {
			sw.setText( R.string.switchSaveOff );
		}
		findViewById( R.id.btnExport ).setOnLongClickListener( v->{
			sp.edit().clear().apply();
			restartApp();
			return true;
		} );

		sw = findViewById( R.id.switchKeepScreenOn );
		sw.setChecked( sp.getBoolean( "keep_screen_on", false ) );
		sw.setOnCheckedChangeListener( (compoundButton, b)->sp.edit().putBoolean( "keep_screen_on", b ).apply() );

		TextView scale = findViewById( R.id.textViewScale );
		scale.setText( String.format( Locale.ROOT, "%d", sp.getInt( "rounding_scale", 8 ) ) );
	}

	private void restartApp() {
		Intent intent = new Intent( this, Main2Activity.class );
		this.startActivity( intent );
		onBackPressed();
	}

	public void changeScaleClickListener(View v) {
		TextView scaleTextView = findViewById( R.id.textViewScale );
		int scale = Integer.parseInt( scaleTextView.getText().toString() );
		int id = v.getId();
		if ( id == R.id.btnMinusScale && scale > 2 ) {
			scale--;
		} else if ( id == R.id.btnPlusScale && scale < 15 ) {
			scale++;
		}
		scaleTextView.setText( String.format( Locale.ROOT, "%d", scale ) );
		sp.edit().putInt( "rounding_scale", scale ).apply();
	}

	public void initializeImport(View v) {
		String fileName = "NewMCalc" + ( mAppType.equals( "dev" ) ? "Dev" : "" ) + ".imc";
		File f = new File( Environment.getExternalStorageDirectory() + "/MST files/" + fileName );
		if ( !f.exists() ) {
			Toast.makeText( getApplicationContext(), R.string.export_file_not_found, Toast.LENGTH_LONG ).show();
			return;
		}
		CustomAlertDialogBuilder builder = new CustomAlertDialogBuilder( this );
		builder.setMessage(
				getResources().getString( R.string.to_continue_need_to_restart_app )
						+ "\n"
						+ getResources().getString( R.string.want_to_continue_question )
		)
				.setCancelable( false ).setPositiveButton( R.string.restart, (dialog, which)->{
			importSettings();
			dialog.cancel();
		} )
				.setNegativeButton( R.string.no, (dialog, which)->dialog.cancel() );
		builder.show();
	}

	private void importSettings() {
		String fileName = "NewMCalc" + ( mAppType.equals( "dev" ) ? "Dev" : "" ) + ".imc";
		File f = new File( Environment.getExternalStorageDirectory() + "/MST files/" + fileName );
		try {
			FileReader fr = new FileReader( f );
			Map<String, ?> m = sp.getAll();
			Set<String> se = m.keySet();
			List<String> l = new ArrayList<>( se );
			ArrayList<Tuple<String, String, String>> a = new ArrayList<>();
			for (int i = 0; i < l.size(); i++) {
				String type = m.get( l.get( i ) ).getClass().getName();
				if ( type.contains( "java.lang." ) ) {
					type = type.replaceAll( "java.lang.", "" );
					a.add( Tuple.create( type, l.get( i ), String.valueOf( m.get( l.get( i ) ) ) ) );
				}
			}
			sp.edit().clear().apply();
			while ( fr.ready() ) {
				char t = (char) fr.read();
				if ( t == '#' ) {
					break;
				}

				if ( t == '\n' ) {
					continue;
				}

				if ( t != 'I' && t != 'B' && t != 'S' ) {
					fr.close();
					sp.edit().clear().apply();
					for (Tuple<String, String, String> p : a) {
						String type = p.first;
						String key = p.second;
						String value = p.third;
						switch ( type ) {
							case "String":
								sp.edit().putString( key, value ).apply();
								break;
							case "Integer":
								sp.edit().putInt( key, Integer.parseInt( value ) ).apply();
								break;
							case "Boolean":
								sp.edit().putBoolean( key, Boolean.parseBoolean( value ) ).apply();
								break;
							case "Float":
								sp.edit().putFloat( key, Float.parseFloat( value ) ).apply();
								break;
							case "Long":
								sp.edit().putLong( key, Long.parseLong( value ) ).apply();
								break;
							default:
								break;
						}
					}
					// TODO: 12.08.2020 somethingWentWrong Activity
					finish();
					return;
				}
				String tag = "";
				char read = (char) fr.read();
				while ( read != '=' ) {
					tag = String.format( "%s%s", tag, read );
					read = (char) fr.read();
				}
				String value = "";
				read = (char) fr.read();
				while ( fr.ready() && read != ( (char) 23 ) ) {
					value = String.format( "%s%c", value, read );
					read = (char) fr.read();
				}
				if ( t == 'S' ) {
					sp.edit().putString( tag, value ).apply();
				} else if ( t == 'I' ) {
					sp.edit().putInt( tag, Integer.parseInt( value ) ).apply();
				} else {
					sp.edit().putBoolean( tag, Boolean.parseBoolean( value ) ).apply();
				}
			}
			fr.close();
			//postcreate();
			restartApp();
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText( getApplicationContext(), e.toString(), Toast.LENGTH_LONG ).show();
		}
	}

	public void export(View v) {
		ProgressDialog pd;
		pd = new ProgressDialog( this );
		pd.setProgressStyle( ProgressDialog.STYLE_SPINNER );
		pd.setCancelable( false );
		File f = new File( Environment.getExternalStorageDirectory() + "/MST files" );
		if ( !f.isDirectory() ) {
			f.mkdir();
		}
		String fileName = "NewMCalc" + ( mAppType.equals( "dev" ) ? "Dev" : "" ) + ".imc";
		f = new File( Environment.getExternalStorageDirectory() + "/MST files/" + fileName );
		pd.show();
		try {
			FileWriter fw = new FileWriter( f, false );
			Map<String, ?> m = sp.getAll();
			fw.write( "" );
			Set<String> se = m.keySet();
			List<String> l = new ArrayList<>( se );
			for (int i = 0; i < l.size(); i++) {
				String ty = m.get( l.get( i ) ).getClass().getName();
				ty = ty.replace( "java.lang.", "" );
				ty = Character.toString( ty.charAt( 0 ) );
				fw.append( ty ).append( l.get( i ) ).append( "=" ).append( String.valueOf( m.get( l.get( i ) ) ) ).append( (char) 23 );
			}
			pd.dismiss();
			fw.append( "\n#Please do not change the values yourself.\n#This can lead to malfunctions or even to malfunction" );
			fw.flush();
			Toast.makeText( getApplicationContext(), R.string.exported, Toast.LENGTH_SHORT ).show();
		} catch (Exception e) {
			pd.dismiss();
			Toast.makeText( getApplicationContext(), e.toString(), Toast.LENGTH_LONG ).show();
			e.printStackTrace();
		}
	}
}
