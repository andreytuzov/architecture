package io.architecture

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.core.content.ContextCompat
import io.architecture.tasks.TasksFragment
import kotlinx.android.synthetic.main.activity_task.*

class TasksActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

        // Set up the toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.run {
            setHomeAsUpIndicator(R.drawable.ic_menu)
            setDisplayHomeAsUpEnabled(true)
        }

        // Set up the navigation drawer
        drawerLayout.setStatusBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
        navigationView.setNavigationItemSelectedListener(::onMenuItemClicked)

        var tasksFragment = supportFragmentManager.findFragmentById(R.id.contentFrame)
        if (tasksFragment == null) {
            tasksFragment = TasksFragment.newInstance()
            supportFragmentManager.beginTransaction()
                .add(R.id.contentFrame, tasksFragment)
                .commit()
        }
    }

    private fun onMenuItemClicked(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.list_navigation_menu_item -> Unit // Do nothing, we're already on that screen
            R.id.statistics_navigation_menu_item -> {
                TODO()
            }
        }
        menuItem.isChecked = true
        drawerLayout.closeDrawers()
        return true
    }
}