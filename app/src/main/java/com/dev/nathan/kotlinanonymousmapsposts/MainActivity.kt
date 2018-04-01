package com.dev.nathan.kotlinanonymousmapsposts

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

import java.util.HashMap

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var mainToolbar: Toolbar? = null
    private lateinit var mAuth: FirebaseAuth
    private var firebaseFirestore: FirebaseFirestore? = null

    private lateinit var current_user_id: String

    private var attestantAddPostBtn: FloatingActionButton? = null

    private var mainbottomNav: BottomNavigationView? = null

    private var attestantFragment: AttestantFragment? = null

    private var mapsFragment: MapsFragment? = null
    lateinit var newsFragment: NewsFragment
    lateinit var relatedFragmentOptionsArea: RelatedFragmentOptionsArea
    lateinit var declarationFragmentOptionsArea: DeclarationFragmentOptionsArea

    lateinit var helpFragment: HelpFragment
    lateinit var placesToAvoidFragment: PlacesToAvoidFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        mAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        mainToolbar = findViewById<Toolbar>(R.id.main_toolbar)
        setSupportActionBar(mainToolbar)


        supportActionBar?.title = getString(R.string.main_page_title)

        mainbottomNav = findViewById(R.id.mainBottomNav)

        // FRAGMENTS
        attestantFragment = AttestantFragment()
        mapsFragment = MapsFragment()
        newsFragment = NewsFragment()
        helpFragment = HelpFragment()
        placesToAvoidFragment = PlacesToAvoidFragment()
        //endregion

        mainbottomNav?.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {

                R.id.bottom_action_map -> {
                    replaceFragment(mapsFragment)
                    true
                }

                R.id.bottom_action_report -> {

                    val newPostIntent = Intent(this@MainActivity, AttestantPostActivity::class.java)
                    startActivity(newPostIntent)

                    true
                }

                R.id.bottom_action_news -> {
                    replaceFragment(attestantFragment)
                    true
                }


                else -> false
            }
        }


        attestantAddPostBtn = findViewById(R.id.attestant_add_post_btn)
        attestantAddPostBtn!!.setOnClickListener(View.OnClickListener { })
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
                this, drawer, mainToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        replaceFragment(mapsFragment)
    }

    override fun onStart() {
        super.onStart()

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            mAuth.signInAnonymously().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this@MainActivity, "deu certo ", Toast.LENGTH_LONG).show()

                }
            }
            //  sendToLogin();

        } else {

            current_user_id = mAuth.currentUser!!.uid

            firebaseFirestore!!.collection("Users").document(current_user_id).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    if (!task.result.exists()) {

                        //                            Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                        //                            startActivity(setupIntent);
                        //                            finish();

                        val userMap = HashMap<String, String>()
                        userMap["name"] = "Anonymous"
                        userMap["image"] = ""

                        firebaseFirestore!!.collection("Users").document(current_user_id).set(userMap as Map<String, Any>)

                    }

                } else {

                    val errorMessage = task.exception?.message
                    Toast.makeText(this@MainActivity, "Error : $errorMessage", Toast.LENGTH_LONG).show()


                }
            }

        }

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.main_menu, menu)
        return true

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return false

    }

    private fun logOut() {


        // mAuth.signOut();

    }


    private fun replaceFragment(fragment: Fragment?) {

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.main_container, fragment)
        fragmentTransaction.commit()

    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when(item.itemId) {

        //region <! principal !>
        //ajuda
            R.id.help_item -> {
                replaceFragment(helpFragment)
            }
        //locais a ser evitados
            R.id.places_to_avoid_item -> {
                replaceFragment(placesToAvoidFragment)
            }
        //endregion

        //region <! relacionados !>
        //informaçoes
            R.id.information_item -> {
                relatedFragmentOptionsArea = RelatedFragmentOptionsArea(getString(R.string.info_title))
                replaceFragment(relatedFragmentOptionsArea)
            }
        //blogs de apoio
            R.id.support_blogs_item -> {
                relatedFragmentOptionsArea = RelatedFragmentOptionsArea(getString(R.string.support_title))
                replaceFragment(relatedFragmentOptionsArea)
            }
        //como se proteger
            R.id.how_to_protect_yourself_item -> {
                relatedFragmentOptionsArea = RelatedFragmentOptionsArea(getString(R.string.protect_title))
                replaceFragment(relatedFragmentOptionsArea)
            }
        //definiçao de especialista
            R.id.specialist_definition_item -> {
                relatedFragmentOptionsArea = RelatedFragmentOptionsArea(getString(R.string.specialist_title))
                replaceFragment(relatedFragmentOptionsArea)
            }
        //tecnicas de prevençao
            R.id.prevention_techniques -> {
                relatedFragmentOptionsArea = RelatedFragmentOptionsArea(getString(R.string.prevention_title))
                replaceFragment(relatedFragmentOptionsArea)
            }

        //endregion

        //region <! noticias !>
        //noticias relacionadas
            R.id.related_news_item -> {
                replaceFragment(newsFragment)
            }
        //midias sociais
            R.id.social_media_item -> {
                replaceFragment(newsFragment)
            }

        //endregion

        //region <! declaraçoes !>
        //noticias relacionadas
            R.id.about_us_item -> {
                declarationFragmentOptionsArea = DeclarationFragmentOptionsArea(getString(R.string.about_us_title))
                replaceFragment(declarationFragmentOptionsArea)
            }
        //midias sociais
            R.id.inspiration_item -> {
                declarationFragmentOptionsArea = DeclarationFragmentOptionsArea(getString(R.string.inspiration_title))
                replaceFragment(declarationFragmentOptionsArea)
            }
        //endregion


        }
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }
}



