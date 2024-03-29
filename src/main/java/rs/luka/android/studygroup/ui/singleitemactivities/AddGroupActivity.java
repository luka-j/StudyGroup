package rs.luka.android.studygroup.ui.singleitemactivities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;

import java.io.File;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.io.Limits;
import rs.luka.android.studygroup.io.LocalImages;
import rs.luka.android.studygroup.io.backgroundtasks.GroupTasks;
import rs.luka.android.studygroup.io.network.Network;
import rs.luka.android.studygroup.misc.Utils;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.ui.Showcase;
import rs.luka.android.studygroup.ui.dialogs.InfoDialog;
import rs.luka.android.studygroup.ui.recyclers.GroupActivity;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

/**
 * Created by luka on 18.7.15..
 */
public class AddGroupActivity extends AppCompatActivity {
    public static final String EXTRA_GROUP            = GroupActivity.EXTRA_GROUP;
    private static final int    INTENT_IMAGE          = 0;
    private static final String STATE_IMAGE_FILE_PATH = "stImage";
    private static final int PERM_REQ_CAMERA          = 1;
    private EditText        name;
    private EditText        place;
    private TextInputLayout nameTil;
    private TextInputLayout placeTil;
    private CardView        add;
    private ImageView       image;
    private CircularProgressView progressView;
    private CheckBox inviteOnly;
    private File imageFile                            = new File(LocalImages.APP_IMAGE_DIR, "groupimage.temp");
    private Group   group;
    private boolean editing;
    private NetworkExceptionHandler exceptionHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);
        initExceptionHandler();

        editing = getIntent().hasExtra(EXTRA_GROUP);

        initToolbar();
        initViews();
        if (editing) {
            group = getIntent().getParcelableExtra(EXTRA_GROUP);
            getSupportActionBar().setTitle(group.getName(this));
            setupViewsForEditing();
        }

        add.setOnClickListener(v -> doSubmit());
        initTextListeners();
        initMediaListeners();

        if(!MaterialShowcaseView.hasAlreadyFired(this, "add-group")) Utils.simulateBackButton();
        new Showcase(this).showSequence("add-group", new View[]{nameTil, inviteOnly},
                                        new int[]{R.string.tut_addgroup_basicinfo, R.string.tut_addgroup_inviteonly});
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_CANCELED) {
            if (requestCode == INTENT_IMAGE) {
                if (data != null && data.getData() != null) { //ako je data==null, fotografija je napravljena kamerom, nije iz galerije
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
                group.edit(this, nameStr, placeStr, inviteOnly.isChecked(), imageFile, exceptionHandler);
            } else {
                GroupTasks.addGroup(this, nameStr, placeStr, inviteOnly.isChecked(), imageFile, exceptionHandler);
            }
            add.setVisibility(View.GONE);
            progressView.setVisibility(View.VISIBLE);
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



    private void initExceptionHandler() {
        exceptionHandler = new NetworkExceptionHandler.DefaultHandler(this) {
            @Override
            public void finishedSuccessfully() {
                super.finishedSuccessfully();
                AddGroupActivity.this.onBackPressed();
            }
            @Override
            public void handleOffline() {
                InfoDialog.newInstance(getString(R.string.error_offline_edit_title),
                                       getString(R.string.error_offline_edit_text))
                          .show(getFragmentManager(), "");
                Network.Status.setOffline();
                progressView.setVisibility(View.GONE);
                add.setVisibility(View.VISIBLE);
            }
            @Override
            public void finishedUnsuccessfully() {
                super.finishedUnsuccessfully();
                progressView.setVisibility(View.GONE);
                add.setVisibility(View.VISIBLE);
            }
        };
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (NavUtils.getParentActivityIntent(this) != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initViews() {
        name = (EditText) findViewById(R.id.add_group_name_input);
        place = (EditText) findViewById(R.id.add_group_place_input);
        nameTil = (TextInputLayout) findViewById(R.id.add_group_name_til);
        placeTil = (TextInputLayout) findViewById(R.id.add_group_place_til);
        add = (CardView) findViewById(R.id.button_add);
        image = (ImageView) findViewById(R.id.add_group_image);
        progressView = (CircularProgressView) findViewById(R.id.add_group_cpv);
        inviteOnly = (CheckBox) findViewById(R.id.add_group_inviteonly_cb);
    }

    private void setupViewsForEditing() {
        name.setText(group.getName(this));
        place.setText(group.getPlace());
        if (group.hasImage()) {
            group.getImage(this,
                           getResources().getDimensionPixelOffset(R.dimen.addview_image_size),
                           exceptionHandler, image);
        }
        name.setSelection(name.getText().length());
    }

    private void initTextListeners() {
        place.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                doSubmit();
                return true;
            }
            return false;
        });
    }

    private void initMediaListeners() {
        image.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                                                  new String[]{Manifest.permission.CAMERA},
                                                  PERM_REQ_CAMERA);
            } else {
                onAddImage(true);
            }
        });
    }

    private boolean askedPermOnce = false;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERM_REQ_CAMERA:
                if(grantResults.length > 0
                   && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA);
                    if(showRationale && !askedPermOnce) {
                        askedPermOnce = true;
                        InfoDialog.newInstance(getString(R.string.explain_perm_camera_title),
                                               getString(R.string.explain_perm_camera_text))
                                  .registerCallbacks(d
                                                             -> ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERM_REQ_CAMERA))
                                  .show(getFragmentManager(), "infoExplainCamera");
                    } else {
                        onAddImage(false);
                    }
                } else if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onAddImage(true);
                }
        }
    }

    private void onAddImage(boolean allowCamera) {
        Intent gallery = new Intent(Intent.ACTION_PICK);
        gallery.setType("image/*");
        if(allowCamera) {
            Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            camera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
            Intent chooserIntent = Intent.createChooser(camera, getString(R.string.select_image));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{gallery});
            startActivityForResult(chooserIntent, INTENT_IMAGE);
        } else {
            startActivityForResult(gallery, INTENT_IMAGE);
        }
    }
}
