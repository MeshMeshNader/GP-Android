package com.grad.gp.Home.Dialogs;

import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.grad.gp.R;


public class ConfirmPerson extends DialogFragment {


    private TextView mPersonName;
    private TextView mPersonRelativeRelation;
    private Button SaveBtn, CancelBtn;
    private ImageView mPersonImage;
    private getDataDialogListener listener;
    private Bitmap bitmap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bitmap = getArguments().getParcelable("image");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.custome_confirm_person, container, false);

        mPersonImage = v.findViewById(R.id.confirm_dialog_person_image);
        mPersonImage.setImageBitmap(bitmap);
        mPersonName = v.findViewById(R.id.confirm_dialog_person_name_et);
        mPersonRelativeRelation = v.findViewById(R.id.confirm_dialog_person_relative_relation_et);
        SaveBtn = v.findViewById(R.id.dialog_save_btn);
        CancelBtn = v.findViewById(R.id.dialog_cancel_btn);


        SaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getActivity(),myAddress.getText().toString(),Toast.LENGTH_LONG).show();
                if (mPersonName.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), "Enter Person Name", Toast.LENGTH_LONG).show();
                } else if (mPersonRelativeRelation.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), "Enter Person Relative Relation", Toast.LENGTH_LONG).show();
                } else {
                    listener.onFinishDialog(mPersonName.getText().toString(), mPersonRelativeRelation.getText().toString(), bitmap);
                }
                dismiss();
            }
        });
        CancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dismiss();
            }
        });
        getDialog().setCanceledOnTouchOutside(true);
        return v;

    }


    public interface getDataDialogListener {
        void onFinishDialog(String name, String relativeRelation, Bitmap bitmap);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the getDataDialogListener so we can send events to the host
            listener = (getDataDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement getDataDialogListener");
        }
    }

}
