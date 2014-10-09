package uk.ac.surrey.sccs.pow.app;

import uk.ac.surrey.sccs.pow.app.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

public class Alert extends DialogFragment {
	
	private boolean kill = true;
	
	public Alert() {
		Bundle args = new Bundle();
        args.putInt("msg", R.string.tsokeError);
        this.setArguments(args);
	}
	
	public static Alert newInstance(int msg, boolean kill) {
		Alert frag = new Alert();
        Bundle args = new Bundle();
        args.putInt("msg", msg);
        frag.setArguments(args);
        frag.kill = kill;
        return frag;
    }
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		int msg = getArguments().getInt("msg");
		
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(msg).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				if (kill){
					Intent startMain = new Intent(Intent.ACTION_MAIN);
					startMain.addCategory(Intent.CATEGORY_HOME);
					startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(startMain);
					getActivity().finish();
				} else {
					// do nothing, just let the user try again
				}
			}
		});
		
		// Create the AlertDialog object and return it
		return builder.create();
	}
	
}
