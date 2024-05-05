package com.example.bondoman

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bondoman.data.TransactionViewModel
import com.example.bondoman.databinding.FragmentListTransactionBinding

class ListTransactionFragment : Fragment() {
    private var _binding: FragmentListTransactionBinding? = null
    val binding get() = _binding!!

    private lateinit var mTransactionViewModel: TransactionViewModel
    private lateinit var adapter: ListTransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentListTransactionBinding.inflate(inflater, container, false)
        val rootView = binding.root

        // RecyclerView
        adapter = ListTransactionAdapter()
        val recyclerView: RecyclerView = binding.listTransaction
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // TransactionViewModel
        mTransactionViewModel = ViewModelProvider(this).get(TransactionViewModel::class.java)
        mTransactionViewModel.getAllTransactions.observe(viewLifecycleOwner, Observer {  transaction ->
            adapter.setData(transaction)
        })

        binding.addButton.setOnClickListener {
            findNavController().navigate(R.id.action_listTransactionFragment_to_addTransactionFragment)
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}