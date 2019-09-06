package com.csr460.iskipper_android;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.csr460.iSkipper.support.IClickerID;

import java.nio.file.FileVisitOption;

public class ManageID extends AppCompatActivity {
    Button addButton;
    EditText newIDText;
    TextView hint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_id);
        addButton = findViewById(R.id.addButton);
        newIDText = findViewById(R.id.newIDText);
        hint = findViewById(R.id.hint);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String string = newIDText.getText().toString();
                IClickerID newId = IClickerID.idFromString(string);
                if(newId == null){
                    hint.setText("please enter valid ID");
                    return;
                }
                Intent result = new Intent();
                result.putExtra("newID", string);
                setResult(1, result);
                finish();
            }
        });
    }
}
