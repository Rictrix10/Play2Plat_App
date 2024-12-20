package com.ddkric.play2plat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView
import com.ddkric.play2plat.R

class PegyAdapter(
    context: Context,
    private val values: Array<String>,
    private val pegiTitle: TextView,
    private val selectedPegi: Int?
) : ArrayAdapter<String>(context, 0, values) {

    private var selectedPosition: Int = -1

    init {
        // Verificar se há um PEGI selecionado inicialmente e definir selectedPosition
        selectedPegi?.let { pegi ->
            selectedPosition = values.indexOf(pegi.toString())
            updatePegiTitle()
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
            R.layout.pegy_list_item,
            parent,
            false
        )
        val textView = view.findViewById<TextView>(R.id.pegy_name)
        val checkBox = view.findViewById<CheckBox>(R.id.pegy_checkbox)

        textView.text = values[position]
        checkBox.isChecked = position == selectedPosition

        // Definir um ouvinte de clique na checkbox
        checkBox.setOnClickListener {
            if (checkBox.isChecked) {
                // Quando a checkbox é marcada, atualize o título do Pegi
                selectedPosition = position
                updatePegiTitle()
                notifyDataSetChanged()
            } else {
                // Quando a checkbox é desmarcada, redefina o título do Pegi Info
                selectedPosition = -1
                pegiTitle.text = "Pegi Information"
                notifyDataSetChanged()
            }
        }

        return view
    }

    private fun updatePegiTitle() {
        pegiTitle.text = "Pegi: ${values[selectedPosition]}"
    }

    fun getSelectedPosition(): Int {
        return selectedPosition
    }
}


