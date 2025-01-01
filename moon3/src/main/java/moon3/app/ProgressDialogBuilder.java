package moon3.app;

import moon3.R;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;


public class ProgressDialogBuilder {
	private MaterialAlertDialogBuilder builder;
	private TextView message;

	public ProgressDialogBuilder(Context c) {
		builder = new MaterialAlertDialogBuilder(c);

		View p = LayoutInflater.from(c).inflate(R.layout.progress_dialog_layout, null, false);

		builder.setView(p);

		message = p.findViewById(R.id.progress_dialog_message);

		message.setText("Please wait...");
	}
	
	public TextView getMessageView() {
		return message;
	}

	public ProgressDialogBuilder setMessage(CharSequence text) {
		message.setText(text);
		return this;
	}
	
	public Dialog create() {
		return builder.create();
	}
	
	public void show() {
		builder.show();
	}

}
