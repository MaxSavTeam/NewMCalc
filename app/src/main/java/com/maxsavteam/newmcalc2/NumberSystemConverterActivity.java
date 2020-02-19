package com.maxsavteam.newmcalc2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.math.BigDecimal;

public class NumberSystemConverterActivity extends AppCompatActivity {
	private String[] data = {"2", "8", "10", "16"};
	private String[] translated_from, translated_to;
	private int fromSys = 10, toSys = 2;
	private EditText fromText, toText;
	private SharedPreferences sp;
	private boolean DarkMode;
	private Point display_size = new Point();

	protected void backPressed(){
		finish();
		overridePendingTransition(R.anim.activity_in1, R.anim.activity_out1);
	}

	@Override
	public void onBackPressed(){
		if (start_type.equals("app")) {
			backPressed();
		} else if (start_type.equals("shortcut")) {
			startActivity(new Intent(this, Main2Activity.class));
			backPressed();
		}
		//super.onBackPressed();
	}
	String start_type;

	private int positionInSet(int sys){
		for(int i = 0; i < data.length; i++){
			String s = data[i];
			if(s.equals(Integer.toString(sys)))
				return i;
		}

		return 10;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if(item.getItemId() == android.R.id.home){
			onBackPressed();
		}
		return super.onOptionsItemSelected(item);
	}

	@SuppressLint("SourceLockedOrientationActivity")
	private void applyTheme(boolean dark_mode){
		ImageButton btn = findViewById(R.id.btnImgCopyNum);
		Display d = getWindowManager().getDefaultDisplay();
		Point p = new Point();
		d.getSize(p);
		ViewGroup.LayoutParams par = btn.getLayoutParams();
		par.width = p.x / 6;
		par.height = p.x / 6;
		btn.setLayoutParams(par);
		ActionBar appActionBar = getSupportActionBar();
		appActionBar.setTitle("");
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		appActionBar.setDisplayHomeAsUpEnabled(true);
		EditText e = findViewById(R.id.edNumTo);
		e.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
		e = findViewById(R.id.edNumFrom);
		e.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
		//appActionBar.setHomeAsUpIndicator(getDrawable(R.drawable.black))
		if(dark_mode){
			getWindow().setBackgroundDrawableResource(R.drawable.black);
			btn.setImageDrawable(getDrawable(R.drawable.ic_content_copy_white_100dp));
			int[] textViewIds = new int[]{R.id.fromSystem, R.id.toSystem, R.id.lblChooseSys};
			for(int id : textViewIds){
				((TextView) findViewById(id)).setTextColor(Color.WHITE);
			}
			appActionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_32dp);
			appActionBar.setBackgroundDrawable(getDrawable(R.drawable.black));
			getWindow().setNavigationBarColor(Color.BLACK);
		}else{
			getWindow().setNavigationBarColor(Color.WHITE);
			btn.setImageDrawable(getDrawable(R.drawable.ic_content_copy_black_100dp));
			getWindow().setBackgroundDrawableResource(R.drawable.white);
			appActionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_32dp);
			appActionBar.setBackgroundDrawable(getDrawable(R.drawable.white));
			//getTheme().applyStyle(R.style.ActionBarLight, true);
		}
		appActionBar.setElevation(0);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		DarkMode = sp.getBoolean("dark_mode", false);
		if(DarkMode){
			setTheme(android.R.style.Theme_Material_NoActionBar);
		}else{
			setTheme(R.style.AppTheme);
		}
		setContentView(R.layout.activity_number_system);
		translated_from = new String[data.length];
		translated_to = new String[data.length];
		Spinner from = findViewById(R.id.chooseFromWhichSys);
		Spinner to = findViewById(R.id.chooseToWhichSys);

		start_type = getIntent().getStringExtra("start_type");
		applyTheme(DarkMode);
		ArrayAdapter<String> ar = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data);
		ar.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		from.setAdapter(ar);
		from.setSelection(2);
		from.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				int choice = Integer.valueOf(data[position]);
				if(choice == toSys){
					toSys = fromSys;
					to.setSelection(positionInSet(toSys));
				}
				fromSys = choice;
				if (fromSys <= 10){
					fromText.setInputType(InputType.TYPE_CLASS_NUMBER);
				}else
					fromText.setInputType(InputType.TYPE_CLASS_TEXT);
				fromText.setText(translated_from[positionInSet(fromSys)]);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
		to.setAdapter(ar);
		to.setSelection(0);
		to.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				//toSys = Integer.valueOf(data[position]);
				int choice = Integer.parseInt(data[position]);
				if(choice == fromSys){
					fromSys = toSys;
					from.setSelection(positionInSet(fromSys));
					fromText.setText(translated_from[positionInSet(fromSys)]);
				}
				toSys = choice;
				//toText.setText(translated_to[position_in_set(toSys)]);
				translate();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		fromText = findViewById(R.id.edNumFrom);
		toText = findViewById(R.id.edNumTo);
		toText.setTextIsSelectable(true);
		fromText.setFocusable(true);
		fromText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(count > 0){
					char last = s.charAt(start);
					if((fromSys == 16 && !(last >= '0' && last <= '9') && !((last >= 'a' && last <= 'f') || (last >= 'A' && last <= 'F')))
						|| (fromSys == 2 && !(last >= '0' && last <= '1'))
						|| (fromSys == 10 && !(last >= '0' && last <= '9'))
						|| (fromSys == 8 && !(last >= '0' && last <= '7'))){
						if(fromText.getText().length() == 1){
							fromText.setText( "" );
							return;
						}
						int curPos = fromText.getSelectionEnd() - 1;
						fromText.setText(s.subSequence( 0, start ).toString().concat( s.subSequence( start + 1, s.length() ).toString()) );
						if(curPos > fromText.getText().length()){
							curPos = fromText.getText().length();
						}
						fromText.setSelection( curPos );
					}
				}
				translate();
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
	}

	private void translate(){
		String from = fromText.getText().toString();
		String to = toText.getText().toString();
		if (!from.equals("")) {
			String result;
			translated_from[positionInSet(fromSys)] = from;
			if(fromSys == 10)
				result = from;
			else
				result = toDecimal(from, fromSys).toPlainString();

			result = fromDecimal(new BigDecimal(result), toSys);
			toText.setText(result);
			translated_to[positionInSet(toSys)] = result;
			//findViewById(R.id.btnImgCopyNum).setVisibility(View.VISIBLE);
		}else{
			toText.setText("");
			findViewById(R.id.btnImgCopyNum).setVisibility(View.INVISIBLE);
		}
	}

	public void copy(View v){
		if(toText.getText().toString().equals(""))
			return;
		android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		clipboard.setText(toText.getText().toString());
		Toast.makeText(getApplicationContext(), getResources().getString(R.string.copied), Toast.LENGTH_SHORT).show();
	}

	private int returnNumber(char c){
		c = Character.toUpperCase(c);
		if(c >= '0' && c <= '9'){
			return Integer.parseInt(Character.toString(c));
		}else if(c >= 'A' && c <= 'Z'){
			return 10 + (((int) c) - ((int) 'A'));
		}
		return '0';
	}
	private char returnCharFromNum(int n){
		if(n >= 0 && n <= 9){
			return ((char) (((int) '0') + n));
		}else {
			return ((char) (((int) 'A') + (n - 10)));
		}
	}

	private BigDecimal toDecimal(String s, final int k){
		BigDecimal x = BigDecimal.ZERO;
		for(int i = 0; i < s.length(); i++){
			x = x.multiply(BigDecimal.valueOf(k));
			x = x.add(BigDecimal.valueOf( returnNumber(s.charAt(i))));
		}
		return x;
	}

	public String fromDecimal(BigDecimal dec, final int k){
		String ans = "";
		do{
			BigDecimal[] bg = dec.divideAndRemainder(BigDecimal.valueOf(k));
			ans = returnCharFromNum(bg[1].intValue()) + ans;
			dec = bg[0];
		}while(dec.signum() > 0);

		return ans;
	}
}