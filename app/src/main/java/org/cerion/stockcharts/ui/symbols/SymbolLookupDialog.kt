package org.cerion.stockcharts.ui.symbols

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.AllCaps
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import org.cerion.stockcharts.common.SymbolAutoCompleteTextView
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SymbolLookupDialog : DialogFragment() {

    private val viewModel: SymbolsViewModel by sharedViewModel()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val input: AutoCompleteTextView = SymbolAutoCompleteTextView(requireContext())
        input.filters = arrayOf<InputFilter>(AllCaps())
        input.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)

        return AlertDialog.Builder(activity).apply {
            setMessage("Enter Symbol")
            setView(input)
            setPositiveButton("OK") { _, _ ->
                viewModel.add(input.text.toString())
            }
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        }.create()
    }
}