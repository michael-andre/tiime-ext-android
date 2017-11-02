package com.cubber.tiime.app

import android.os.Bundle
import android.support.annotation.IdRes
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import com.cubber.tiime.R
import com.cubber.tiime.app.allowances.AllowancesFragment
import com.cubber.tiime.app.wages.WagesFragment
import kotlinx.android.synthetic.main.main_activity.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var drawerToggle: ActionBarDrawerToggle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        navigation_view.setNavigationItemSelectedListener(this)

        if (savedInstanceState == null) {
            showSection(R.id.mileage_allowances)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (item.groupId == R.id.sections) {
            showSection(item.itemId)
            drawer.closeDrawer(GravityCompat.START)
        }
        return true
    }

    private fun showSection(@IdRes id: Int) {
        when (id) {
            R.id.mileage_allowances -> showSection(AllowancesFragment::class.java)
            R.id.social -> showSection(WagesFragment::class.java)
        }

    }

    private fun showSection(fragmentClass: Class<out Fragment>) {
        try {
            val fm = supportFragmentManager
            val fragment = fm.findFragmentById(R.id.content)
            if (fragment != null && fragmentClass == fragment.javaClass) {
                return
            }
            fm.beginTransaction()
                    .replace(R.id.content, fragmentClass.newInstance())
                    .commit()
        } catch (e: Exception) {
            Log.e(MainActivity::class.java.simpleName, "Failed to show " + fragmentClass.simpleName, e)
        }

    }

    override fun setSupportActionBar(toolbar: Toolbar?) {
        super.setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false)
            var toggle = drawerToggle
            if (toggle != null) {
                drawer.removeDrawerListener(toggle)
            }
            toggle = ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
            drawer.addDrawerListener(toggle)
            toggle.syncState()
            drawerToggle = toggle
        }
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

}
