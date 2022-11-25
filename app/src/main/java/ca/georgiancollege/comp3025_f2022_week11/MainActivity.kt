package ca.georgiancollege.comp3025_f2022_week11

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var TVShows: MutableList<TVShow>

    lateinit var addTVShowFAB: FloatingActionButton
    lateinit var tvShowAdapter: TVShowAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // initialization
        database = Firebase.database.reference
        TVShows = mutableListOf<TVShow>() // creates an empty List container
        tvShowAdapter = TVShowAdapter(TVShows)

        tvShowAdapter.onTVShowClick = { tvShow, position ->
            showCreateTVShowDialog(AlertAction.UPDATE, tvShow, position)
        }

        tvShowAdapter.onTVShowSwipeLeft = {tvShow, position ->
            showCreateTVShowDialog(AlertAction.DELETE, tvShow, position)
        }

        initializeRecyclerView()
        initializeFAB()
        addTVShowEventListener(database)
    }


    private fun initializeFAB() {
        addTVShowFAB = findViewById(R.id.add_TV_Show_FAB)
        addTVShowFAB.setOnClickListener {
            showCreateTVShowDialog(AlertAction.ADD, null, null)
        }
    }

    private fun initializeRecyclerView() {
        val recyclerView: RecyclerView = findViewById(R.id.First_Recycler_View)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = tvShowAdapter
    }

    fun writeNewTVShow(tvShow: TVShow)
    {
        database.child("TVShows").child(tvShow.id.toString()).setValue(tvShow)
    }

    fun updateTVShow(tvShow: TVShow)
    {
        database.child("TVShows").child(tvShow.id.toString()).setValue(tvShow)
    }

    fun deleteTVShow(tvShow: TVShow?)
    {
        database.child("TVShows").child(tvShow?.id.toString()).removeValue()
    }

    private fun showCreateTVShowDialog(alertAction: AlertAction, tvShow: TVShow?, position: Int?) {
        var dialogTitle: String = ""
        var positiveButtonTitle: String = ""
        var negativeButtonTitle: String = getString(R.string.cancel)

        when (alertAction) {
            AlertAction.ADD -> {
                dialogTitle = getString(R.string.add_dialog_title)
                positiveButtonTitle = getString(R.string.add_tv_show)
            }
            AlertAction.UPDATE -> {
                dialogTitle = getString(R.string.update_dialog_title)
                positiveButtonTitle = getString(R.string.update_tv_show)
            }
            AlertAction.DELETE -> {
                dialogTitle = ""
                positiveButtonTitle = getString(R.string.delete_tv_show)
            }
        }

        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.add_new_tv_show_item, null)

        builder.setTitle(dialogTitle)
        builder.setView(view)

        val tvShowTitleTextView = view.findViewById<TextView>(R.id.tv_show_title_TextView)
        val tvShowTitleEditText = view.findViewById<EditText>(R.id.tv_show_title_EditText)
        val studioNameTextView = view.findViewById<TextView>(R.id.studio_name_TextView)
        val studioNameEditText = view.findViewById<EditText>(R.id.studio_name_EditText)

        when(alertAction)
        {
            AlertAction.ADD -> {
                builder.setPositiveButton(positiveButtonTitle) { dialog, _ ->
                    dialog.dismiss()
                    val firstCharTitle = tvShowTitleEditText.text.toString().substring(0,1)
                    val firstCharStudio = studioNameEditText.text.toString().substring(0,1)
                    val id = firstCharTitle + firstCharStudio + System.currentTimeMillis().toString()
                    val newTVShow = TVShow(id, tvShowTitleEditText.text.toString(), studioNameEditText.text.toString())
                    writeNewTVShow(newTVShow)
                }
            }
            AlertAction.UPDATE -> {

                if (tvShow != null) {
                    tvShowTitleEditText.setText(tvShow?.title)
                    studioNameEditText.setText(tvShow?.studio)
                }

                builder.setPositiveButton(positiveButtonTitle) { dialog, _ ->
                    dialog.dismiss()
                    val newTVShow = TVShow(tvShow?.id, tvShowTitleEditText.text.toString(), studioNameEditText.text.toString())
                    updateTVShow(newTVShow)
                }
            }
            AlertAction.DELETE -> {
                tvShowTitleTextView.setText("Delete " + tvShow?.title)
                tvShowTitleTextView.setTextColor(ContextCompat.getColor(view.context, R.color.red))
                tvShowTitleEditText.isVisible = false
                studioNameTextView.setText(R.string.are_you_sure_prompt)
                studioNameEditText.isVisible = false

                builder.setPositiveButton(positiveButtonTitle) { dialog, _ ->
                    dialog.dismiss()
                    deleteTVShow(tvShow)
                }

                builder.setNegativeButton(negativeButtonTitle) {dialog, _ ->
                    dialog.cancel()
                }
            }
        }
        builder.create().show()
    }

    private fun addTVShowEventListener(dbReference: DatabaseReference)
    {
        val TVShowListener = object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                TVShows.clear()
                val tvShowDB = dataSnapshot.child("TVShows").children

                for(tvShow in tvShowDB)
                {
                    var newShow = tvShow.getValue(TVShow::class.java)

                    if(newShow != null)
                    {
                        TVShows.add(newShow)
                        tvShowAdapter.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("tvShowError", "loadTVShow:cancelled", databaseError.toException())
            }
        }
        dbReference.addValueEventListener(TVShowListener)
    }

}