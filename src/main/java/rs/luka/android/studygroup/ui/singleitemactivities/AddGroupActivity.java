package rs.luka.android.studygroup.ui.singleitemactivities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.io.DataManager;
import rs.luka.android.studygroup.io.Limits;
import rs.luka.android.studygroup.io.LocalImages;
import rs.luka.android.studygroup.misc.Utils;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.ui.recyclers.GroupActivity;

/**
 * Created by luka on 18.7.15..
 */
public class AddGroupActivity extends AppCompatActivity {
    private static final int    INTENT_IMAGE          = 0;
    private static final String STATE_IMAGE_FILE_PATH = "stImage";
    private EditText        name;
    private EditText        place;
    private TextInputLayout nameTil;
    private TextInputLayout placeTil;
    private CardView        add;
    private ImageView       image;
    private File imageFile = new File(LocalImages.APP_IMAGE_DIR, "groupimage.temp");
    private Group   group;
    private boolean editing;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);

        group = getIntent().getParcelableExtra(GroupActivity.EXTRA_GROUP);
        editing = group != null;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (NavUtils.getParentActivityIntent(this) != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); //because reasons
        }

        name = (EditText) findViewById(R.id.add_group_name_input);
        place = (EditText) findViewById(R.id.add_group_place_input);
        nameTil = (TextInputLayout) findViewById(R.id.add_group_name_til);
        placeTil = (TextInputLayout) findViewById(R.id.add_group_place_til);
        add = (CardView) findViewById(R.id.button_add);
        image = (ImageView) findViewById(R.id.add_group_image);
        if (editing) {
            name.setText(group.getName());
            place.setText(group.getPlace());
            if (group.hasImage()) {
                image.setImageBitmap(group.getImage(getResources().getDimensionPixelOffset(R.dimen.addview_image_size)));
            }
            name.setSelection(name.getText().length());
        }

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSubmit();
            }
        });
        place.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    doSubmit();
                    return true;
                }
                return false;
            }
        });
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Intent gallery = new Intent(Intent.ACTION_PICK);
                gallery.setType("image/*");
                camera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
                Intent chooserIntent = Intent.createChooser(camera,
                                                            getString(R.string.select_image));
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{gallery});
                startActivityForResult(chooserIntent, INTENT_IMAGE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_CANCELED) {
            if (requestCode == INTENT_IMAGE) {
                if (data
                    != null) { //ako je data==null, fotografija je napravljena kamerom, nije iz galerije
                    imageFile = new File(Utils.getRealPathFromUri(this, data.getData()));
                }
                image.setImageBitmap(LocalImages.loadImage(imageFile,
                                                           getResources().getDimensionPixelSize(R.dimen.addview_image_size)));
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_IMAGE_FILE_PATH, imageFile.getAbsolutePath());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.getString(STATE_IMAGE_FILE_PATH) != null) {
            imageFile = new File(savedInstanceState.getString(STATE_IMAGE_FILE_PATH));
            image.setImageBitmap(LocalImages.loadImage(imageFile,
                                                       getResources().getDimensionPixelOffset(R.dimen.addview_image_size)));
        }
    }

    private void doSubmit() {
        boolean error = false;
        String nameStr = name.getText().toString(),
                placeStr = place.getText().toString();
        if (nameStr.isEmpty()) {
            nameTil.setError(getString(R.string.error_empty));
            error = true;
        } else if (nameStr.length() >= Limits.GROUP_NAME_MAX_LENGTH) {
            nameTil.setError(getString(R.string.error_too_long));
            error = true;
        } else { nameTil.setError(null); }
        if (placeStr.length() >= Limits.GROUP_PLACE_MAX_LENGTH) {
            placeTil.setError(getString(R.string.error_too_long));
            error = true;
        } else { placeTil.setError(null); }
        if (!error) {
            if (editing) {
                group.edit(this, nameStr, placeStr, imageFile);
            } else {
                DataManager.addGroup(this, nameStr, placeStr, imageFile);
            }
            this.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
