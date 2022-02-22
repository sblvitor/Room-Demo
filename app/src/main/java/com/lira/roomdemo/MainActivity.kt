package com.lira.roomdemo

import android.app.AlertDialog
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.lira.roomdemo.databinding.ActivityMainBinding
import com.lira.roomdemo.databinding.DialogUpdateBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val employeeDao = (application as EmployeeApp).db.employeeDao()

        binding?.btnAdd?.setOnClickListener {
            addRecord(employeeDao)
        }

        lifecycleScope.launch{
            employeeDao.fetchAllEmployees().collect {
                val list = ArrayList(it)
                setupListOfDataIntoRecyclerView(list, employeeDao)
            }
        }

    }

    fun addRecord(employeeDao: EmployeeDao){
        val name = binding?.etName?.text.toString()
        val email = binding?.etEmailID?.text.toString()

        if(name.isNotEmpty() && email.isNotEmpty()){
            lifecycleScope.launch {
                employeeDao.insert(EmployeeEntity(name=name, email=email))
                Toast.makeText(applicationContext, "Record saved", Toast.LENGTH_SHORT).show()
                binding?.etName?.text?.clear()
                binding?.etEmailID?.text?.clear()
            }
        }else{
            Toast.makeText(applicationContext, "Name or Email cannot be blank", Toast.LENGTH_SHORT).show()
        }

    }

    private fun setupListOfDataIntoRecyclerView(employeesList: ArrayList<EmployeeEntity>, employeeDao: EmployeeDao){

        if(employeesList.isNotEmpty()){
            val itemAdapter = ItemAdapter(employeesList,
                { updateId ->
                    updateRecordDialog(updateId, employeeDao)
                },
                { deleteId ->
                    deleteRecordAlertDialog(deleteId, employeeDao)
                })
            binding?.rvItemsList?.layoutManager = LinearLayoutManager(this)
            binding?.rvItemsList?.adapter = itemAdapter
            binding?.rvItemsList?.visibility = View.VISIBLE
            binding?.tvNoneInserted?.visibility = View.GONE
        }else{
            binding?.rvItemsList?.visibility = View.GONE
            binding?.tvNoneInserted?.visibility = View.VISIBLE
        }

    }

    private fun updateRecordDialog(id: Int, employeeDao: EmployeeDao){

        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)
        val binding = DialogUpdateBinding.inflate(layoutInflater)
        updateDialog.setContentView(binding.root)

        lifecycleScope.launch {
            employeeDao.fetchEmployeeById(id).collect {
                if(it != null){
                    binding.etNameUpdate.setText(it.name)
                    binding.etEmailUpdate.setText(it.email)
                }
            }
        }

        binding.tvBtnUpdate.setOnClickListener {
            val name = binding.etNameUpdate.text.toString()
            val email = binding.etEmailUpdate.text.toString()
            if(name.isNotEmpty() && email.isNotEmpty()){
                lifecycleScope.launch {
                    employeeDao.update(EmployeeEntity(id, name, email))
                    Toast.makeText(applicationContext, "Record updated", Toast.LENGTH_SHORT).show()
                    updateDialog.dismiss()
                }
            }else{
                Toast.makeText(applicationContext, "Name or Email cannot be blank", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvBtnCancel.setOnClickListener {
            updateDialog.dismiss()
        }

        updateDialog.show()

    }

    private fun deleteRecordAlertDialog(id: Int, employeeDao: EmployeeDao){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Record?")

        builder.setPositiveButton("Yes"){ dialogInterface, _ ->
            lifecycleScope.launch {
                employeeDao.delete(EmployeeEntity(id))
                Toast.makeText(applicationContext, "Record deleted!", Toast.LENGTH_SHORT).show()
                dialogInterface.dismiss()
            }
        }

        builder.setNegativeButton("No"){ dialogInterface, _ ->
            dialogInterface.dismiss()
        }

        builder.create()
        builder.setCancelable(false).show()
    }

}