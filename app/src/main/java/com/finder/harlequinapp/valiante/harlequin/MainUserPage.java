package com.finder.harlequinapp.valiante.harlequin;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.balysv.materialripple.MaterialRippleLayout;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.haha.perflib.Main;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.truizlop.fabreveallayout.FABRevealLayout;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import uk.co.chrisjenx.calligraphy.CalligraphyUtils;

import static android.view.Gravity.CENTER;

public class MainUserPage extends AppCompatActivity {


    protected  DatabaseReference myDatabase;
    protected DatabaseReference mDatabaseLike;
    protected TabLayout tabs;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private FloatingActionButton fab;
    private CoordinatorLayout mCoordinatorLayout;
    private NavigationView navigationView;
    public Snackbar mSnackbar;
    private View navHeader;
    private TextView txtName,txtCity;
    private ImageView imgNavHeaderBg;
    private CircularImageView imgProfile;
    protected DatabaseReference userReference;
    protected static boolean isSingle = true;
    protected static boolean isMale = true;
    public static Integer userAge;
    private ValueEventListener mUserDataListener;
    protected static String userId = null;
    protected static User userClass;
    protected String myuserName = null;
    protected CollapsingToolbarLayout collapseLayout;
    protected CircularImageView collapseProfile;
    protected ImageView copertina;


    private static final String urlNavHeaderBg = "http://www.magic4walls.com/wp-content/uploads/2015/01/abstract-colored-lines-red-material-design-triangles-lilac-background1.jpg";
    // index to identify current nav menu item


    // tags used to attach the fragments
    private static final String TAG_HOME = "home";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user_page);
        Toolbar toolbar = (Toolbar)findViewById(R.id.main_toolbar);



        final Typeface tf = Typeface.createFromAsset(MainUserPage.this.getAssets(), "fonts/Hero.otf");


        copertina = (ImageView)findViewById(R.id.copertina);
        collapseProfile = (CircularImageView)findViewById(R.id.circular_collapse_profile);

        collapseLayout = (CollapsingToolbarLayout)findViewById(R.id.collapsing_toolbar_userpage);
        collapseLayout.setCollapsedTitleTypeface(tf);
        collapseLayout.setExpandedTitleTypeface(tf);

        mCoordinatorLayout = (CoordinatorLayout)findViewById(R.id.main_content);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        navigationView = (NavigationView)findViewById(R.id.nav_view);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,R.string.toggle_opened,R.string.toggle_closed);
        fab = (FloatingActionButton)findViewById(R.id.faboulous);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        //elementi del navigation Header
        navHeader = navigationView.getHeaderView(0);
        txtName = (TextView) navHeader.findViewById(R.id.name);
        imgNavHeaderBg = (ImageView) navHeader.findViewById(R.id.img_header_bg);
        imgProfile = (CircularImageView) navHeader.findViewById(R.id.drawerAvatar);
        txtCity = (TextView)navHeader.findViewById(R.id.navigationCity);

        //carica gli elementi del navigation Drawer
        loadNavigationHeader();
        myDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabaseLike = myDatabase.child("Likes");
        myDatabase.keepSynced(true);

        Typeface steinerLight = Typeface.createFromAsset(getAssets(),"fonts/Steinerlight.ttf");



        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(ContextCompat.getDrawable(this, R.drawable.vector_burger_menu_24));


        //Viewpager per i fragment
        ViewPager viewPager = (ViewPager)findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        //tablayout per i fragment
        tabs = (TabLayout)findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position){

                    case(1):
                        updatedToolbarTitle("Ciao coglione");
                        break;
                    case (2):
                        updatedToolbarTitle("Ciao minchione");
                    default:
                         updatedToolbarTitle("Ciao "+myuserName);


                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //per cambiare il font nel tablayout
        ViewGroup vg = (ViewGroup) tabs.getChildAt(0);
        changeFontInViewGroup(vg,"fonts/Hero.otf");
        mSnackbar= Snackbar.make(mCoordinatorLayout, "LUL",Snackbar.LENGTH_SHORT);


        //Onclick per il navigation Drawer
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_item1:
                        Toast.makeText(MainUserPage.this,"Ciaone",Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        return false;
                }
                return false;
            }
        });


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createEvent(view);

            }
        });
    }//fine OnCreate

    //per il Fragment degli eventi principali
    public static class MyEventViewHolder extends RecyclerView.ViewHolder{

        View mView;
        CircularImageView cardProfile;
        TextView cardLikes,cardDate,cardTime;
        ImageButton cardLike,chatRoomBtn;
        TextView event_name,partecipanti,etaMedia,sexDistribution,cardPrice;
        FABRevealLayout mFABRevealLayout;
        Button chiudi;
        FloatingActionButton fabLike;


        //costruttore del View Holder personalizzato
        public MyEventViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
            //Elementi UI per la carta evento

            cardPrice = (TextView)mView.findViewById(R.id.cardPrice);
            partecipanti = (TextView)mView.findViewById(R.id.partecipanti);
            etaMedia = (TextView)mView.findViewById(R.id.eta_media);
           sexDistribution = (TextView)mView.findViewById(R.id.sex_distribution);
            cardDate = (TextView)mView.findViewById(R.id.cardDay);
            cardTime = (TextView)mView.findViewById(R.id.cardTime);
            mFABRevealLayout = (FABRevealLayout)mView.findViewById(R.id.fab_reveal);
            chiudi = (Button)mView.findViewById(R.id.closeInfo);
            fabLike = (FloatingActionButton)mView.findViewById(R.id.fabLike);

        }

        public  void setCardPrice (Integer price, Boolean isFree){
            if(isFree){
                cardPrice.setText("Free entry");
            }
            if(!isFree){
                cardPrice.setText("Ingresso "+price+"€");
            }
        }
        public void revealFabInfo(Integer eta,Integer numeroPartecipanti,Integer maleLikes, Integer femaleLikes){
            if(numeroPartecipanti !=0) {
                if(numeroPartecipanti == 1){
                    partecipanti.setText(1+ " Partecipante");
                }
                else {
                    partecipanti.setText(numeroPartecipanti + " Partecipanti");
                }
                etaMedia.setText("Età media : " + eta);
                sexDistribution.setText(getMalePercentage(numeroPartecipanti,maleLikes)+"% Uomini      "+
                                        getFemalePercentage(numeroPartecipanti,femaleLikes)+"% Donne");
            }
            else{
                partecipanti.setText("");
                etaMedia.setGravity(CENTER);
                etaMedia.setText("Non ci sono ancora partecipanti a questo evento");
                sexDistribution.setText("");
            }
        }
        //metodi necessari per visualizzare dinamicamente i dati di ogni EventCard
        public void setThumbUp (){
            fabLike.setImageResource(R.drawable.white_star_empty_24);
        }
        public void setThumbDown (){
            fabLike.setImageResource(R.drawable.white_star_full_24);
        }
        //TODO fa vedere i like correnti, da migliorare
        public void setLikes (Integer likes){cardLikes.setText("Partecipanti: "+likes); }
        public void setCreatorAvatar (final Context avatarctx , final String creatorAvatarPath){
            Picasso.with(avatarctx)
                    .load(creatorAvatarPath)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(cardProfile, new Callback() {
                        @Override
                        public void onSuccess() {
                            //va bene così non deve fare nulla
                        }
                        @Override
                        public void onError() {
                            Picasso.with(avatarctx).load(creatorAvatarPath).into(cardProfile);
                        }
                    });
        }
        public void setEventName (String eventName){
            event_name = (TextView)mView.findViewById(R.id.CardViewTitle);
            event_name.setText(eventName);
        }
        /*
        public void setDescription (String description){
            TextView event_desc = (TextView)mView.findViewById(R.id.CardViewDescription);
            event_desc.setText(description);
        }*/
        public void setEventImage (final Context ctx, final String eventImagePath){
            final ImageView event_image = (ImageView)mView.findViewById(R.id.CardViewImage);

            Picasso.with(ctx)
                    .load(eventImagePath)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(event_image, new Callback() {
                        @Override
                        public void onSuccess() {
                            //va bene così non deve fare nulla
                        }
                        @Override
                        public void onError() {
                            Picasso.with(ctx).load(eventImagePath).into(event_image);
                        }
                    });
        }
        public void setCardDate (String eventDate){
            cardDate.setText(eventDate);
        }
        public void setCardTime (String eventTime) { cardTime.setText(eventTime);}


        //Helper methods
        public Float getMalePercentage (Integer totalLikes, Integer maleLikes){
            Float malePercentage ;
            malePercentage = Float.valueOf((100 * maleLikes) / totalLikes);
            return malePercentage;
        }

        public Float getFemalePercentage (Integer totalLikes, Integer femaleLikes){
            Float femalePercentage;
            femalePercentage = Float.valueOf((100*femaleLikes)/totalLikes);
            return femalePercentage;
        }


    }//[END]eventViewHolder

    //Inizio Favorite Viewholder
    public static class FavouritesViewHolder extends RecyclerView.ViewHolder {

        View mView;
        CircularImageView eventAvatar;
        TextView eventName,eventDate,eventTime;
        ImageButton cardLikeButton;

        public FavouritesViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            eventAvatar = (CircularImageView)mView.findViewById(R.id.fav_event_avatar);
            eventName   = (TextView)mView.findViewById(R.id.fav_event_name);
            eventDate   = (TextView)mView.findViewById(R.id.fav_event_date);
            eventTime   = (TextView)mView.findViewById(R.id.fav_event_time);
            cardLikeButton = (ImageButton)mView.findViewById(R.id.cardLikeButton);
        }

        public void setAvatar (final Context avatarctx, final String avatarUrl){
            Picasso.with(avatarctx)
                    .load(avatarUrl)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(eventAvatar, new Callback() {
                        @Override
                        public void onSuccess() {
                            //va bene così non deve fare nulla
                        }
                        @Override
                        public void onError() {
                            Picasso.with(avatarctx).load(avatarUrl).into(eventAvatar);
                        }
                    });

        }
        public void setName (String name){
            eventName.setText(name);
        }

        public void setTime (String time){
            eventTime.setText(time);
        }

        public void setEventDate (String date){
            eventDate.setText(date);
        }
    }
    //[FINE FavouritesEventViewHolder

    @Override
    protected void onStart() {
        super.onStart();

        //prende l'utente in firebase
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        //se l'utente  correttamente loggato
        if(currentUser !=null) {
            userId = currentUser.getUid();
            if (!userId.isEmpty()) {
                userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
            }
        }

        //listener per caricare dalla toolBar
        ValueEventListener userDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User myuser = dataSnapshot.getValue(User.class);
                userClass = myuser;
                myuserName = myuser.getUserName();
                String relationshipStatus = myuser.getUserRelationship();
                final String avatarUrl = myuser.getProfileImage();
                String userName = myuser.getUserName()+" "+myuser.getUserSurname();
                String userCity = myuser.getUserCity();
                txtCity.setText(userCity);
                txtName.setText(userName);



                //controlla il sesso
                String userGender = myuser.getUserGender();
                if(userGender.equalsIgnoreCase("Uomo")){
                    //va bene così isMale è settato di default su maschio
                }
                if(userGender.equalsIgnoreCase("Donna")){
                    isMale = false;
                }



                Picasso.with(getApplicationContext())
                       .load(avatarUrl)
                       .networkPolicy(NetworkPolicy.OFFLINE)
                       .into(collapseProfile, new Callback() {
                           @Override
                           public void onSuccess() {
                               //tutto ok
                           }

                           @Override
                           public void onError() {
                               Picasso.with(getApplicationContext())
                                       .load(avatarUrl)
                                       .into(collapseProfile);
                           }
                       });

                Picasso.with(getApplicationContext())
                        .load(avatarUrl)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(imgProfile, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(getApplicationContext())
                                        .load(avatarUrl)
                                        .into(imgProfile);
                            }
                        });
                updatedToolbarTitle("Ciao "+myuserName );


                //imposta situazione sentimentale
                if(relationshipStatus.equalsIgnoreCase("Impegnato")
                        || relationshipStatus.equalsIgnoreCase("Impegnata")){
                    isSingle = false;
                }

                //imposta il sesso
                if (myuser.getUserGender().equalsIgnoreCase("Female")){
                    isMale = false;
                }
                userAge = getAge(myuser.getUserAge());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        //reference per renderlo removibile
        mUserDataListener = userDataListener;
        //preleva nome dall'Auth personalizzando l'actionBar e i dati principali dell'utente
        myDatabase.child("Users").child(userId).addValueEventListener(userDataListener);
    }

    //qui vengono rimossi i listener dell'activity
    @Override
    protected void onStop() {
        super.onStop();
        myDatabase.child("Users").child(userId).removeEventListener(mUserDataListener);
    }



    // Add Fragments to Tabs
    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(new EventFragment(), "Eventi");
        adapter.addFragment(new ChatFragment(), "Chat");
        adapter.addFragment(new FavouritesFragment(), "Favoriti");
        viewPager.setAdapter(adapter);
    }

    static class Adapter extends FragmentPagerAdapter{
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

    }

    //per la libreria Calligraphy
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    //per cambiare il font nella toolBar
    void changeFontInViewGroup(ViewGroup viewGroup, String fontPath) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (TextView.class.isAssignableFrom(child.getClass())) {

                CalligraphyUtils.applyFontToTextView(child.getContext(), (TextView) child, fontPath);
            } else if (ViewGroup.class.isAssignableFrom(child.getClass())) {
                changeFontInViewGroup((ViewGroup) viewGroup.getChildAt(i), fontPath);
            }
        }
    }

    //menu del Navigation Drawer
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(drawerToggle.onOptionsItemSelected(item)){

                    return true;

        }
         return super.onOptionsItemSelected(item);
    }

    //carica l'immagine nel drawer
    private void loadNavigationHeader(){

        Picasso.with(getApplicationContext())
                .load(urlNavHeaderBg)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(imgNavHeaderBg, new Callback() {
                    @Override
                    public void onSuccess() {
                        //
                    }

                    @Override
                    public void onError() {
                        Picasso.with(getApplicationContext()).load(urlNavHeaderBg).into(imgNavHeaderBg);
                    }
                });
    }

    //manda alla creazione dell'evento
    public void createEvent(View view) {
        Intent switchToCreateEvent = new Intent(getApplication(), CreateEvent.class);
        startActivity(switchToCreateEvent);
    }

    //calcola l'età da String a Integer
    public Integer getAge (String birthdate){
        //estrae i numeri dalla stringa
        String parts [] = birthdate.split("/");
        //li casta in interi
        Integer day = Integer.parseInt(parts[0]);
        Integer month = Integer.parseInt(parts[1]);
        Integer year = Integer.parseInt(parts[2]);

        //oggetto per l'anno di nascita
        Calendar dob = Calendar.getInstance();
        //oggetto per l'anno corrente
        Calendar today = Calendar.getInstance();

        //setta anno di nascita in formato data
        dob.set(year,month,day);
        //calcola l'anno
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        //controlla che il giorno attuale sia minore del giorno del compleanno
        //nel caso in cui fosse vero allora il compleanno non è ancora passato e il conteggio degli anni viene diminuito
        if (today.get(Calendar.DAY_OF_YEAR)<dob.get(Calendar.DAY_OF_YEAR)){
            age--;
        }

        //restituisce l'età sotto forma numerica utile per calcolare l'età media dei partecipanti ad un evento
        return age;

    }

    //cambia il titolo della toolbar, accessibile dai fragments
    protected void updatedToolbarTitle(String title){
        collapseLayout.setTitle(title);
    }






}
