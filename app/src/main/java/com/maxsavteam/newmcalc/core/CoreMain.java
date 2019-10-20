package com.maxsavteam.newmcalc.core;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.maxsavteam.newmcalc.R;
import com.maxsavteam.newmcalc.error.Error;
import com.maxsavteam.newmcalc.error.MultException;
import com.maxsavteam.newmcalc.utils.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public final class CoreMain {
	private CoreLinkBridge coreLinkBridge;
	private boolean was_error = false;
	private Resources res;
	private String invalid_argument, value_is_too_big, division_by_zero;
	private Context c;

	public CoreMain(Context context) {
		this.res = context.getApplicationContext().getResources();
		this.c = context;
		invalid_argument = res.getString(R.string.invalid_argument);
		value_is_too_big = res.getString(R.string.value_is_too_big);
		division_by_zero = res.getString(R.string.division_by_zero);
	}

	public void setInterface(CoreLinkBridge clb){
		this.coreLinkBridge = clb;
	}
	
	public interface CoreLinkBridge{
		void onSuccess(BigDecimal result, String type);
		void onError(Error error);
	}

	private static Stack<String> s0 = new Stack<>();
	private static Stack<BigDecimal> s1 = new Stack<>();

	private final int MAX_FACTORIAL_VALUE = 1000;
	private final BigDecimal MAX_POW = new BigDecimal("1000");

	public void prepare(String example, String type){
		int len = example.length();
		char last;
		if(len == 0)
			return;
		else
			last = example.charAt(len - 1);

		if(last == '(')
			return;

		int brackets = 0;
		for(int i = 0; i < example.length(); i++){
			if(example.charAt(i) == '(')
				brackets++;
			else if(example.charAt(i) == ')')
				brackets--;
		}
		if(brackets > 0){
			for(int i = 0; i < brackets; i++){
				example += ")";
			}
		}
		if(example.contains(" ")){
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < example.length(); i++) {
				if (example.charAt(i) != ' ')
					sb.append(example.charAt(i));
			}
			example = sb.toString();
		}
		try{
			BigDecimal b = null;
			b = new BigDecimal(example);
			if(b != null){
				coreLinkBridge.onError(new Error().setStatus("Core").setError("/Core/String is number"));
				return;
			}
		}catch (NumberFormatException e){
			Log.e("All ok", e.toString());
		}
		if(example.contains(res.getString(R.string.multiply)) || example.contains(res.getString(R.string.div))
				|| example.contains(res.getString(R.string.pi)) || example.contains(res.getString(R.string.fi))
				|| example.contains(res.getString(R.string.sqrt))){
			char[] mas = example.toCharArray();
			String p;

			for(int i = 0; i < example.length(); i++){
				p = Character.toString(mas[i]);
				if(p.equals(res.getString(R.string.div))){
					mas[i] = '/';
				}else if(p.equals(res.getString(R.string.multiply))){
					mas[i] = '*';
				}else if(p.equals(res.getString(R.string.pi))){
					mas[i] = 'P';
				}else if(p.equals(res.getString(R.string.fi))){
					mas[i] = 'F';
				}else if(p.equals(res.getString(R.string.sqrt))){
					mas[i] = 'R';
				}
			}
			example = new String(mas);
		}
		calc(example, type);
	}

	private void calc(String example, String type){
		s0.clear();
		s1.clear();
		was_error = false;
		char[] str = new char[example.length()];
		example.getChars(0, example.length(), str, 0);
		String x;
		String s;
		int len = example.length();

		for(int i = 0; i < len; i++){
			try {
				s = Character.toString(str[i]);
				if (s.equals("s") || s.equals("t") || s.equals("l") || s.equals("c")) {
					if (i != 0) {
						if (example.charAt(i - 1) == ')') {
							s0.push("*");
						}
					}
					//if(i + 4 <= example.length()){
					String let = "";
					while (i < example.length() && Utils.islet(example.charAt(i))) {
						let += Character.toString(example.charAt(i));
						i++;
					}
					switch (let) {
						case "sin":
							s0.push("sin");
							s0.push("(");
							break;
						case "cos":
							s0.push("cos");
							s0.push("(");
							break;
						case "tan":
							s0.push("tag");
							s0.push("(");
							break;
						case "log":
							s0.push("log");
							s0.push("(");
							break;
						case "ln":
							s0.push("ln");
							s0.push("(");
							break;
					}
					continue;
				}
				if (s.equals("P")) {
					BigDecimal f = new BigDecimal(Math.PI);
					s1.push(f);
					if (i != 0 && Utils.isDigit(example.charAt(i - 1))) {
						in_s0('*');
					}
					char next = '\0';
					if (i != example.length() - 1)
						next = example.charAt(i + 1);
					if (i != example.length() - 1 && (Utils.isDigit(example.charAt(i + 1)) || next == 'F' || next == 'P' || next == 'e')) {
						in_s0('*');
					}
					//s1.push(f);
					continue;
				} else if (s.equals("F")) {
					BigDecimal f = new BigDecimal(1.618);
					s1.push(f);
					if (i != 0 && Utils.isDigit(example.charAt(i - 1))) {
						in_s0('*');
					}
					char next = '\0';
					if (i != example.length() - 1)
						next = example.charAt(i + 1);
					if (i != example.length() - 1 && (Utils.isDigit(example.charAt(i + 1)) || next == 'F' || next == 'P' || next == 'e')) {
						in_s0('*');
					}
					continue;
				} else if (s.equals("!")) {
					try {
						if (i != len - 1 && example.charAt(i + 1) == '!') {
							BigDecimal y = s1.peek(), ans = BigDecimal.ONE;
							if (y.signum() < 0 || y.compareTo(BigDecimal.valueOf(MAX_FACTORIAL_VALUE)) > 0) {

								was_error = true;
								if (y.compareTo(BigDecimal.valueOf(MAX_FACTORIAL_VALUE)) > 0)
									coreLinkBridge.onError(new Error().setError("Invalid argument: factorial value is too much").setShort_error(value_is_too_big)); // I do not know how to name this error
								break;
							}
							for (; y.compareTo(BigDecimal.valueOf(0)) > 0; y = y.subtract(BigDecimal.valueOf(2))) {
								ans = ans.multiply(y);
							}
							i++;
							s1.pop();
							s1.push(ans);
							continue;
						} else {
							BigDecimal y = s1.peek();
							if (y.signum() == -1) {
								was_error = true;
								coreLinkBridge.onError(new Error().setError("Error: Unable to find negative factorial.").setShort_error(invalid_argument));
								break;
							} else {
								if (y.compareTo(new BigDecimal(MAX_FACTORIAL_VALUE)) > 0) {
									was_error = true;
									coreLinkBridge.onError(new Error().setError("For some reason, we cannot calculate the factorial of this number " +
											"(because it is too large and may not have enough device resources when executed)").setShort_error(value_is_too_big));
									break;
								} else {
									s1.pop();
									String fa = y.toString();
									if (fa.contains(".")) {
										int index = fa.lastIndexOf(".");
										fa = fa.substring(0, index);
										y = new BigDecimal(fa);
									}
									s1.push(Utils.fact(y));
								}
							}
							if (i != len - 1) {
								char next = example.charAt(i + 1);
								if (Utils.isDigit(next) || next == 'P' || next == 'F' || next == 'e')
									in_s0('*');
							}
							continue;
						}
					} catch (Exception e) {
						e.printStackTrace();
						was_error = true;
						coreLinkBridge.onError(new Error().setError(e.toString()).setMessage(e.getMessage()).setShort_error(value_is_too_big));
						break;
					}
				} else if (s.equals("%")) {
					try {

						/*class Isolated implements CoreLinkBridge{
							private BigDecimal res;

							public Error getError() {
								return error;
							}

							public boolean isWas_error() {
								return was_error;
							}

							private Error error;
							private boolean was_error = false;

							public BigDecimal getRes() {
								return res;
							}

							@Override
							public void onSuccess(BigDecimal result, String type) {
								res = result;
							}

							@Override
							public void onError(Error error) {
								was_error = true;
								this.error = error;
							}

							private void run(String ex){
								CoreMain core = new CoreMain(c);
								core.setInterface(this);

								core.prepare(ex, "");
							}
						}
						i++;
						String x1 = "";
						while(i < example.length() && example.charAt(i) != '-' && example.charAt(i) != '+'){
							x1 += example.charAt(i);
							i++;
						}
						x1 = s1.peek().toPlainString() + x1;
						Isolated isolated = new Isolated();
						isolated.run(x1);
						if(isolated.isWas_error()) {
							was_error = true;
							coreLinkBridge.onError(isolated.getError());
							return;
						}else{
							BigDecimal top = s1.peek();
							s1.pop();
							top = isolated.getRes();
							top = top.divide(BigDecimal.valueOf(100), RoundingMode.HALF_EVEN);
							top = new BigDecimal(Utils.delete_zeros(top.toPlainString()));
							s1.push(top);
						}*/
						BigDecimal y = s1.peek();
						s1.pop();
						y = y.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_EVEN);
						y = new BigDecimal(Utils.delete_zeros(y.toPlainString()));
						s1.push(y);
						if (i != len - 1) {
							char next = example.charAt(i + 1);
							if (Utils.isDigit(next) || next == 'P' || next == 'F' || next == 'e')
								in_s0('*');
						}
						continue;
					} catch (Exception e) {
						e.printStackTrace();
						was_error = true;
						coreLinkBridge.onError(new Error().setError(e.toString()).setMessage(e.getMessage()).setShort_error(value_is_too_big));
						break;
					}
				} else if (s.equals("e")) {
					BigDecimal f = new BigDecimal(Math.E);
					s1.push(f);
					if (i != 0 && Utils.isDigit(example.charAt(i - 1))) {
						in_s0('*');
					}
					if (i != example.length() - 1 && Utils.isDigit(example.charAt(i + 1))) {
						in_s0('*');
					}
					continue;
				} else if (s.equals("R")) {
					if (i == len - 1) {
						was_error = true;
						break;
					} else {
						if (example.charAt(i + 1) == '(') {
							in_s0('R');
							continue;
						} else {
							was_error = true;
							coreLinkBridge.onError(new Error().setError("Invalid statement for square root").setShort_error(invalid_argument));
							break;
						}
					}
				} else if (s.equals("A")) {
					i += 2;
					String n = "";
					int actions = 0;
					while (example.charAt(i) != ')') {
						if (example.charAt(i) == '+') {
							actions++;
							s1.push(new BigDecimal(n));
							n = "";
						} else {
							n += Character.toString(example.charAt(i));
						}
						i++;
					}
					s1.push(new BigDecimal(n));
					BigDecimal sum = BigDecimal.ZERO;
					for (int j = 0; j <= actions; j++) {
						sum = sum.add(s1.peek());
						s1.pop();
					}
					sum = sum.divide(BigDecimal.valueOf(actions + 1), 2, RoundingMode.HALF_EVEN);
					String answer = sum.toPlainString();
					s1.push(new BigDecimal(Utils.delete_zeros(answer)));
					continue;
				} else if (s.equals("G")) {
					i += 2;
					String n = "";
					int actions = 0;
					while (example.charAt(i) != ')') {
						if (example.charAt(i) == '*') {
							actions++;
							s1.push(new BigDecimal(n));
							n = "";
						} else {
							n += Character.toString(example.charAt(i));
						}
						i++;
					}
					s1.push(new BigDecimal(n));
					BigDecimal sum = BigDecimal.ONE;
					for (int j = 0; j <= actions; j++) {
						sum = sum.multiply(s1.peek());
						s1.pop();
					}
					sum = BigDecimal.valueOf(Math.sqrt(sum.doubleValue())); // BigDecimal has method BigDecimal.abs(), but it is available in Java 9 and high, Android uses Java 8
					String answer = sum.toPlainString();
					s1.push(new BigDecimal(Utils.delete_zeros(answer)));
					continue;
				}
				if (Utils.isDigit(str[i])) {
					x = "";
					while ((i < example.length()) && ((example.charAt(i) == '.') || Utils.isDigit(str[i]) || (example.charAt(i) == '-' && example.charAt(i - 1) == 'E'))) {
						s = Character.toString(str[i]);
						x += s;
						i++;
					}
					BigDecimal temp = new BigDecimal(x);
					if (temp.divide(BigDecimal.ONE, 2, RoundingMode.HALF_DOWN).equals(BigDecimal.valueOf(3.14))) {
						x = Double.toString(Math.PI);
					} else if (temp.divide(BigDecimal.ONE, 3, RoundingMode.FLOOR).equals(BigDecimal.valueOf(1.618))) {
						x = "1.618";
					}
					s1.push(new BigDecimal(x));
					if (i < example.length() && str[i] == 'E') {
						in_s0('^');
						i++;
						BigDecimal t = BigDecimal.ONE;
						if (str[i] == '+') {
							t = BigDecimal.ONE;
						} else if (str[i] == '-') {
							t = BigDecimal.valueOf(-1.0);
						}
						x = "";
						while (i < example.length() && (example.charAt(i) == '.' || Utils.isDigit(example.charAt(i)))) {
							x += Character.toString(example.charAt(i));
							i++;
						}
						s1.push(new BigDecimal(x).multiply(t));
					}
					i--;
				} else {
					if (str[i] != ')') {
						if (str[i] == '^') {
							if (i != example.length() - 1 && str[i + 1] == '(') {
								i++;
								in_s0('^');
								s0.push("(");
								continue;
							} else if (i != example.length() - 1 && str[i + 1] != '(') {
								//i++;
								in_s0('^');
								continue;
							}
						}
						if ((i == 0 && str[i] == '-') || (str[i] == '-' && example.charAt(i - 1) == '(')) {
							x = "";
							i++;
							while ((i < example.length()) && ((example.charAt(i) == '.') || Utils.isDigit(str[i]) || example.charAt(i) == 'E' || (example.charAt(i) == '-' && example.charAt(i - 1) == 'E'))) {
								s = Character.toString(str[i]);
								x += s;
								i++;
							}
							i--;
							s1.push(new BigDecimal(x).multiply(BigDecimal.valueOf(-1)));
							continue;
						}
						if (i != 0 && str[i] == '(' && (Utils.isDigit(str[i - 1]) || str[i - 1] == ')')) {
							in_s0('*');
						}

						in_s0(str[i]);
					} else {
						while (!s0.empty() && !s0.peek().equals("(")) {
							mult(s0.peek());
							if (was_error)
								break;
							s0.pop();
						}
						if (!s0.empty() && s0.peek().equals("(")) {
							s0.pop();
						}
						if (i != example.length() - 1) {
							if (Utils.isDigit(example.charAt(i + 1))) {
								in_s0('*');
							}
						}
					}
				}
			}catch (MultException e){
				was_error = true;
				coreLinkBridge.onError(new Error().setStatus("Core").setShort_error("Smth went wrong"));
				break;
			}
		}
		try {
			while (!was_error && !s0.isEmpty() && s1.size() >= 2) {
				mult(s0.peek());
				if (was_error)
					break;
				s0.pop();
			}
			if (!was_error && !s0.isEmpty() && s1.size() == 1) {
				if (s0.peek().equals("R")) {
					mult(s0.peek());
					s0.pop();
				}
				if (!s0.isEmpty() && (s0.peek().equals("cos") || s0.peek().equals("sin") || s0.peek().equals("log") || s0.peek().equals("ln") || s0.peek().equals("tan"))) {
					mult(s0.peek());
					s0.pop();
				}
			}
		}catch (Exception e) {
			was_error = true;
			coreLinkBridge.onError(new Error().setStatus("Core"));
		}
		if(!was_error){
			coreLinkBridge.onSuccess(s1.peek(), type);
		}
	}

	private void mult(String x) throws MultException {
		try {
			if (x.length() == 3 || x.equals("ln") || x.equals("R")) {
				double d = s1.peek().doubleValue();
				 BigDecimal ans = BigDecimal.ONE;
				if (x.equals("log") && d <= 0) {
					//Toast.makeText(getApplicationContext(), "You cannot find the logarithm of a zero or a negative number.", Toast.LENGTH_SHORT).show();
					was_error = true;
					coreLinkBridge.onError(new Error().setError("You cannot find the logarithm of a zero or a negative number.").setShort_error(invalid_argument));
					return;
				}
				s1.pop();
				switch (x) {
					case "cos": {
						ans = BigDecimal.valueOf(Math.cos(d));
						break;
					}
					case "sin": {
						ans = BigDecimal.valueOf(Math.sin(d));
						break;
					}
					case "tan": {
						ans = BigDecimal.valueOf(Math.tan(d));
						break;
					}
					case "log": {
						if (d <= 0) {
							was_error = true;
							coreLinkBridge.onError(new Error().setError("Illegal argument: unable to find log of " + (d == 0 ? "zero." : "negative number.")).setShort_error(invalid_argument));
							return;
						}
						ans = BigDecimal.valueOf(Math.log10(d));
						break;
					}
					case "ln": {
						if (d <= 0) {
							was_error = true;
							coreLinkBridge.onError(new Error().setError("Illegal argument: unable to find ln of " + (d == 0 ? "zero." : "negative number.")).setShort_error(invalid_argument));
							return;
						}
						ans = BigDecimal.valueOf(Math.log(d));
						break;
					}
					case "R":
						if (d < 0) {
							was_error = true;
							coreLinkBridge.onError(new Error().setError("Invalid argument: the root expression cannot be negative.").setShort_error(invalid_argument));
							return;
						}
						ans = BigDecimal.valueOf(Math.sqrt(d));
						break;
				}
				ans = ans.divide(BigDecimal.valueOf(1.0), 9, RoundingMode.HALF_EVEN);
				String answer = ans.toPlainString();
				s1.push(new BigDecimal(Utils.delete_zeros(answer)));
				return;
			}
			BigDecimal b = s1.peek();
			s1.pop();
			BigDecimal a = s1.peek();
			BigDecimal ans = s1.peek();
			s1.pop();
			double a1, b1, ansd = Double.POSITIVE_INFINITY;
			a1 = a.doubleValue();
			b1 = b.doubleValue();
			try {
				switch (x) {
					case "+":
						ans = a.add(b);
						break;
					case "-":
						ans = a.subtract(b);
						break;
					case "*":
						ans = a.multiply(b);
						break;
					case "/":
						if (b.signum() == 0) {
							was_error = true;
							coreLinkBridge.onError(new Error().setError("Division by zero.").setShort_error(division_by_zero));
							return;
						}
						ans = a.divide(b);
						break;
					case "^":
						if(b.compareTo(MAX_POW) > 0){
							was_error = true;
							coreLinkBridge.onError(new Error().setShort_error(value_is_too_big));
							return;
						}
						ans = BigDecimal.valueOf(Math.pow(a1, b1));
						break;
				}
				String answer = ans.toPlainString();
				ans = new BigDecimal(Utils.delete_zeros(answer));
				s1.push(ans);
			} catch (ArithmeticException e) {
				String str = e.toString();
				if (str.contains("Non-terminating decimal expansion; no exact representable decimal result")) {
					ans = a.divide(b, 3, RoundingMode.HALF_EVEN);
					ans = new BigDecimal(Utils.delete_zeros(ans.toPlainString()));
					s1.push(ans);
				} else {
					was_error = true;
					coreLinkBridge.onError(new Error().setError(e.toString()).setMessage(e.getMessage()));
				}
			}
		}catch(EmptyStackException e){
			was_error = true;
			coreLinkBridge.onError(new Error().setStatus("Core").setError(e.toString()));
		}catch (Exception e) {
			was_error = true;
			coreLinkBridge.onError(new Error().setError(e.toString()).setMessage(e.getMessage()));
			throw new MultException(e.getMessage());
		}
	}

	private void in_s0(char x) throws MultException{
		Map<String, Integer> priority = new HashMap<>();
		priority.put("(", 0);
		priority.put("-", 1);
		priority.put("+", 1);
		priority.put("/", 2);
		priority.put("*", 2);
		priority.put("^", 3);
		priority.put("R", 3);
		if(x == '('){
			s0.push(Character.toString(x));
			return;
		}
		if(s0.empty()){
			s0.push(Character.toString(x));
			return;
		}

		if(!s0.empty() && s0.peek().equals("(")){
			s0.push(Character.toString(x));
			return;
		}
		if (!s0.empty() && (priority.get(Character.toString(x)) < priority.get(s0.peek()) || priority.get(Character.toString(x)).equals(priority.get(s0.peek())))) {
			mult(s0.peek());
			s0.pop();
			in_s0(x);
			return;
		}
		if(!s0.empty() && priority.get(Character.toString(x)) > priority.get(s0.peek()))
			s0.push(Character.toString(x));
	}
	
}
