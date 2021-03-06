package bumblebees.hobee;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import bumblebees.hobee.hobbycategories.HobbiesChoiceActivity;
import bumblebees.hobee.objects.Hobby;
import bumblebees.hobee.objects.User;
import bumblebees.hobee.utilities.CropSquareTransformation;
import bumblebees.hobee.utilities.SessionManager;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class UserProfileActivity extends AppCompatActivity {
    private Gson gson = new Gson();
    private User user;
    private LinearLayout hobbyContainer;
    private String extra;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        extra = getIntent().getStringExtra("User");

        TextView userName = (TextView) findViewById(R.id.userName);

        TextView userAge = (TextView) findViewById(R.id.userAge);
        ImageView userGender = (ImageView) findViewById(R.id.userGender);
        TextView userBiography = (TextView) findViewById(R.id.userBiography);
        ImageView userImage = (ImageView) findViewById(R.id.userImage);
        TextView globalRank = (TextView) findViewById(R.id.globalRank);
        TextView hostRank = (TextView) findViewById(R.id.hostRank);
        TextView noShows = (TextView) findViewById(R.id.noShows);
        ImageView editProfile = (ImageView) findViewById(R.id.editProfileBtn);
        ImageView editHobbies = (ImageView) findViewById(R.id.editHobbiesBtn);
        hobbyContainer = (LinearLayout) findViewById(R.id.profileHobbyContainer);


        // If user wants to see his own profile

        if (extra == null) {
            try {
                SessionManager session = new SessionManager(this);
                user=session.getUser();

            } catch (NullPointerException e) {
                Toast toast = Toast.makeText(getApplicationContext(), "Error seeing profile.", Toast.LENGTH_LONG);
                toast.show();
                finish();
            }

        } else if (extra.equals("error")) {
            Toast toast = Toast.makeText(getApplicationContext(), "Error seeing profile.", Toast.LENGTH_LONG);
            toast.show();
            finish();

            // View other profile

        } else {
            editHobbies.setVisibility(View.INVISIBLE);
            editProfile.setVisibility(View.INVISIBLE);
            user = gson.fromJson(getIntent().getStringExtra("User"), User.class);
        }

        try {
            userName.setText(user.getFirstName() + " " + user.getLastName());
            userAge.setText("" + user.getAge());
            if (user.getGender().equals("male")) {
                userGender.setImageResource(R.drawable.gender_male);
            }
            else {
                userGender.setImageResource(R.drawable.gender_female);
            }
            globalRank.setText(reputationToRank(user.getRank().getGlobalRep()));
            hostRank.setText(reputationToRank(user.getRank().getHostRep()));
            noShows.setText(Integer.toString(user.getRank().getNoShows()));
            userBiography.setText(user.getBio());
            Picasso.with(this).load(user.getPicUrl(getApplicationContext())).transform(new CropSquareTransformation()).into(userImage);
            showHobbies((ArrayList<Hobby>) user.getHobbies());
        }
        catch (NullPointerException e) {
            Toast toast = Toast.makeText(getApplicationContext(), "Error seeing profile.", Toast.LENGTH_LONG);
            toast.show();
            finish();
        }




        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editProfileIntent = new Intent(getApplicationContext(), RegisterUserActivity.class);
                editProfileIntent.putExtra("Source", "UserProfileActivity");
                startActivity(editProfileIntent);
            }
        });

        editHobbies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editHobbiesintent = new Intent(getApplicationContext(), HobbiesChoiceActivity.class);
                editHobbiesintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(editHobbiesintent);
            }
        });
    }

    private String reputationToRank(int reputation) {
        if (reputation < -7000) {
            return "-3";
        }
        if (reputation > -7000 && reputation <= -2500) {
            return "-2";
        }
        if (reputation > -2500 && reputation <= -1000) {
            return "-1";
        }
        if (reputation > -1000 && reputation <= 1000) {
            return "0";
        }
        if (reputation > 1000 && reputation <= 2500) {
            return "+1";
        }
        if (reputation > 2500 && reputation <= 7000) {
            return "+2";
        }
        if (reputation > 7000) {
            return "+3";
        } else {
            return "err";
        }
    }

    @Override
    public void onBackPressed() {
        if (extra == null) {
            Intent backPressIntent = new Intent(UserProfileActivity.this, HomeActivity.class);
            startActivity(backPressIntent);
        }
        else {
            finish();
        }
    }

    /**
     * Fill in the hobbies that the user has on their profile.
     * @param hobbies - array of hobbies
     */
    private void showHobbies(ArrayList<Hobby> hobbies){
        for(Hobby hobby : hobbies) {
            LayoutInflater inflater = LayoutInflater.from(this);
            final View hobbyView = inflater.inflate(R.layout.profile_hobby_item, hobbyContainer, false);
            ImageView hobbyImage = (ImageView) hobbyView.findViewById(R.id.profileHobbyIcon);
            TextView hobbyName = (TextView) hobbyView.findViewById(R.id.profileHobbyName);
            SeekBar hobbySkill = (SeekBar) hobbyView.findViewById(R.id.profileHobbySkill);
            //do nothing when the user tries to move the seekbar
            hobbySkill.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return true;
                }
            });

            hobbyImage.setImageResource(hobby.getIcon());
            hobbyName.setText(hobby.getName());
            switch (hobby.getDifficultyLevel()){
                case "Beginner":
                    hobbySkill.setProgress(0);
                    break;
                case "Intermediate":
                    hobbySkill.setProgress(1);
                    break;
                case "Expert":
                    hobbySkill.setProgress(2);
                    break;
                default:
                    hobbySkill.setProgress(0);
            }
            hobbyContainer.addView(hobbyView);
        }

    }




}



