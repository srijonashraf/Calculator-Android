package com.example.calculator;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView displayField;
    private StringBuilder expressionBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        displayField = findViewById(R.id.displayField);
        expressionBuilder = new StringBuilder();

        setButtonClickListeners();
    }

    private void setButtonClickListeners() {
        // Numbers
        setNumberButtonClickListener(R.id.btn0);
        setNumberButtonClickListener(R.id.btn1);
        setNumberButtonClickListener(R.id.btn2);
        setNumberButtonClickListener(R.id.btn3);
        setNumberButtonClickListener(R.id.btn4);
        setNumberButtonClickListener(R.id.btn5);
        setNumberButtonClickListener(R.id.btn6);
        setNumberButtonClickListener(R.id.btn7);
        setNumberButtonClickListener(R.id.btn8);
        setNumberButtonClickListener(R.id.btn9);

        // Operators
        setOperatorButtonClickListener(R.id.btnAdd);
        setOperatorButtonClickListener(R.id.btnSubtract);
        setOperatorButtonClickListener(R.id.btnMultiply);
        setOperatorButtonClickListener(R.id.btnDivide);

        // Other Buttons
        setOtherButtonClickListener(R.id.btnDot);
        setOtherButtonClickListener(R.id.btnOneByDivide);
        setOtherButtonClickListener(R.id.btnPower);
        setOtherButtonClickListener(R.id.btnCross);

        // Clear Button
        Button btnClear = findViewById(R.id.btnClear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expressionBuilder.setLength(0);
                displayField.setText("0");
            }
        });

        // Equal Button
        Button btnEqual = findViewById(R.id.btnEqual);
        btnEqual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculate();
            }
        });
    }

    private void setNumberButtonClickListener(int buttonId) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String buttonText = button.getText().toString();
                expressionBuilder.append(buttonText);
                displayField.setText(expressionBuilder.toString());
            }
        });
    }

    private void setOperatorButtonClickListener(int buttonId) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String buttonText = button.getText().toString();
                expressionBuilder.append(" ").append(buttonText).append(" ");
                displayField.setText(expressionBuilder.toString());
            }
        });
    }

    private void setOtherButtonClickListener(int buttonId) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String buttonText = button.getText().toString();

                if (buttonText.equals("X") && expressionBuilder.length() > 0) {
                    expressionBuilder.setLength(expressionBuilder.length() - 1);
                } else if (buttonText.equals("x^")) {
                    expressionBuilder.append("^");
                } else if (buttonText.equals("1/x")) {
                    String lastNumber = getLastNumber(expressionBuilder.toString());
                    if (!lastNumber.isEmpty()) {
                        double number = Double.parseDouble(lastNumber);
                        double reciprocal = 1 / number;
                        expressionBuilder.setLength(expressionBuilder.length() - lastNumber.length());
                        expressionBuilder.append(reciprocal);
                        displayField.setText(expressionBuilder.toString());
                        return;
                    }
                }
                else if (buttonText.equals(".")) {
                    expressionBuilder.append(".");
                }

                displayField.setText(expressionBuilder.toString());
            }
        });
    }

    private String getLastNumber(String expression) {
        String[] parts = expression.split("[-+*/^]");
        if (parts.length > 0) {
            return parts[parts.length - 1];
        }
        return "";
    }

    private void calculate() {
        String expression = expressionBuilder.toString();
        if (!expression.isEmpty()) {
            try {
                double result = evaluateExpression(expression);
                displayField.setText(String.valueOf(result));
            } catch (IllegalArgumentException e) {
                displayField.setText("Invalid expression");
            }
        }
    }

    private double evaluateExpression(String expression) {
        return new ExpressionEvaluator().evaluate(expression);
    }

    private class ExpressionEvaluator {
        public double evaluate(String expression) {
            return new Object() {
                int pos = -1, ch;

                void nextChar() {
                    ch = (++pos < expression.length()) ? expression.charAt(pos) : -1;
                }

                boolean eat(int charToEat) {
                    while (ch == ' ') nextChar();
                    if (ch == charToEat) {
                        nextChar();
                        return true;
                    }
                    return false;
                }

                double parse() {
                    nextChar();
                    double x = parseExpression();
                    if (pos < expression.length())
                        throw new IllegalArgumentException("Invalid expression: " + expression);
                    return x;
                }

                double parseExpression() {
                    double x = parseTerm();
                    for (; ; ) {
                        if (eat('+')) x += parseTerm();
                        else if (eat('-')) x -= parseTerm();
                        else return x;
                    }
                }

                double parseTerm() {
                    double x = parseFactor();
                    for (; ; ) {
                        if (eat('*')) x *= parseFactor();
                        else if (eat('/')) x /= parseFactor();
                        else return x;
                    }
                }

                double parseFactor() {
                    if (eat('+')) return parseFactor();
                    if (eat('-')) return -parseFactor();

                    double x;
                    int startPos = this.pos;
                    if (eat('(')) {
                        x = parseExpression();
                        eat(')');
                    } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                        while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                        x = Double.parseDouble(expression.substring(startPos, this.pos));
                    } else {
                        throw new IllegalArgumentException("Invalid expression: " + expression);
                    }

                    if (eat('^')) {
                        double exponent = parseFactor();
                        x = Math.pow(x, exponent);
                    }

                    return x;
                }
            }.parse();
        }
    }
}
