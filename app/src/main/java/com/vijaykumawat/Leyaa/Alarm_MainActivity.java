package com.vijaykumawat.Leyaa;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vijaykumawat.Leyaa.data.AlarmReminderContract;
import com.vijaykumawat.Leyaa.data.AlarmReminderDbHelper;

// Note: Removed unused imports (ProgressDialog, ImageView) for cleaner code.
public class Alarm_MainActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {

        private FloatingActionButton mAddReminderButton;
        // Removed unused member mToolbar
        AlarmCursorAdapter mCursorAdapter;
        AlarmReminderDbHelper alarmReminderDbHelper = new AlarmReminderDbHelper(this);
        ListView reminderListView;
        // Removed unused member prgDialog
        TextView reminderText;

        // Class member for the empty view container, used with setEmptyView()
        View emptyView;

        private String alarmTitle = "";

        private static final int VEHICLE_LOADER = 0;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);

                // 1. Find views and set up header text
                reminderListView = findViewById(R.id.list);
                reminderText = findViewById(R.id.reminderText);

                // 2. Set up the empty view. This is the key to automatic visibility control.
                emptyView = findViewById(R.id.empty_view);
                reminderListView.setEmptyView(emptyView);

                // 3. Set up the CursorAdapter
                mCursorAdapter = new AlarmCursorAdapter(this, null);
                reminderListView.setAdapter(mCursorAdapter);

                // 4. Set up item click listener to edit existing reminders
                reminderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                                Intent intent = new Intent(Alarm_MainActivity.this, AddReminderActivity.class);

                                // Build the URI for the specific reminder row
                                Uri currentVehicleUri = ContentUris.withAppendedId(AlarmReminderContract.AlarmReminderEntry.CONTENT_URI, id);

                                intent.setData(currentVehicleUri);
                                startActivity(intent);
                        }
                });

                // 5. Set up Floating Action Button to add new reminders
                mAddReminderButton = findViewById(R.id.fab);
                mAddReminderButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                addReminderTitle();
                        }
                });

                // 6. Initialize the Loader to fetch data asynchronously
                getSupportLoaderManager().initLoader(VEHICLE_LOADER, null, this);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
                String[] projection = {
                        AlarmReminderContract.AlarmReminderEntry._ID,
                        AlarmReminderContract.AlarmReminderEntry.KEY_TITLE,
                        AlarmReminderContract.AlarmReminderEntry.KEY_DATE,
                        AlarmReminderContract.AlarmReminderEntry.KEY_TIME,
                        AlarmReminderContract.AlarmReminderEntry.KEY_ACTIVE
                };

                // Use the CursorLoader to query the Content Provider on a background thread
                return new CursorLoader(this,
                        AlarmReminderContract.AlarmReminderEntry.CONTENT_URI, // Provider content URI to query
                        projection,             // Columns to include
                        null,                   // No selection clause
                        null,                   // No selection arguments
                        null);                  // Default sort order
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                // Swap the new cursor into the adapter.
                // This automatically triggers the ListView to check if the adapter is empty,
                // which then controls the visibility of 'emptyView'.
                mCursorAdapter.swapCursor(cursor);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
                // Clear the adapter when the loader is reset
                mCursorAdapter.swapCursor(null);
        }

        // Dialog to prompt the user for the reminder title
        public void addReminderTitle() {

                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialog));
                builder.setTitle("Set Reminder Title");

                final EditText input = new EditText(this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                                String title = input.getText().toString().trim();
                                if (title.isEmpty()) {
                                        Toast.makeText(getApplicationContext(), "Reminder title cannot be empty", Toast.LENGTH_SHORT).show();
                                        return;
                                }

                                alarmTitle = title;
                                ContentValues values = new ContentValues();
                                values.put(AlarmReminderContract.AlarmReminderEntry.KEY_TITLE, alarmTitle);

                                Uri newUri = getContentResolver().insert(AlarmReminderContract.AlarmReminderEntry.CONTENT_URI, values);

                                restartLoader();

                                if (newUri == null) {
                                        Toast.makeText(getApplicationContext(), "Setting Reminder Title failed", Toast.LENGTH_SHORT).show();
                                } else {
                                        // Navigate or inform the user about the next step (setting date/time)
                                        Toast.makeText(getApplicationContext(), "Pending: Set Reminder Date", Toast.LENGTH_SHORT).show();
                                        // Optional: You might want to open AddReminderActivity here for date/time
                                        // Intent intent = new Intent(Alarm_MainActivity.this, AddReminderActivity.class);
                                        // intent.setData(newUri);
                                        // startActivity(intent);
                                }
                        }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                        }
                });

                builder.show();
        }

        public void restartLoader() {
                // Forces the Loader to run again and fetch fresh data from the database
                getSupportLoaderManager().restartLoader(VEHICLE_LOADER, null, this);
        }

        @Override
        int getContentViewId() {
                return R.layout.reminder_activity_main; // Assuming this maps to your XML layout
        }

        @Override
        int getNavigationMenuItemId() {
                return R.id.navigation_reminder;
        }
}