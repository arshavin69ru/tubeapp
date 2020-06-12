package com.app.tubeapp.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.app.tubeapp.R
import com.app.tubeapp.viewmodels.SharedViewModel

class DownloadDialogFragment : DialogFragment() {

    private lateinit var listView: ListView
    private val model: SharedViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(model)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_download_dialog, container, false)
    }

    // instantiate list view
    private fun initListView(view: View, listAdapter: ArrayAdapter<String>, listener: AdapterView.OnItemClickListener) {
        listView = view.findViewById(R.id.formatListView)
        listView.adapter = listAdapter
        listView.onItemClickListener = listener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // start observing for selected formats.
        model.getVideoFormatList()?.observe(this, Observer {
            initListView(
                view,
                CustomListAdapter(requireContext(), it, R.layout.fragment_download_dialog),
                AdapterView.OnItemClickListener { _, view, _, _ ->
                    val v = view.findViewById<TextView>(R.id.format_text)
                    // get the text of the item that was clicked.
                    model.setSelection(v.text.toString())
                    // close the dialog.
                    this.dismiss()
                })
        })
    }
}
// custom ArrayAdapter class used to hold data for list view.
class CustomListAdapter(private val ctx: Context, private val listOfFormats: ArrayList<String>?, resource: Int) :
    ArrayAdapter<String>(ctx, resource, listOfFormats!!) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var temp = convertView
        val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        if (temp == null) temp = inflater.inflate(R.layout.row_item, parent, false)
        temp?.findViewById<TextView>(R.id.format_text)?.text = listOfFormats!![position]
        return temp!!
    }

    override fun getItem(position: Int): String? {
        return listOfFormats?.get(position)
    }
}