package com.denisu.homesorter.view

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.denisu.homesorter.R
import com.denisu.homesorter.controller.ContainerAlarmManager
import com.denisu.homesorter.model.Container
import java.util.*

class NotificationAddActivity : AppCompatActivity() {

    private lateinit var messageEditText: EditText
    private lateinit var datePickerButton: Button
    private lateinit var timePickerButton: Button
    private lateinit var datePickerText: TextView
    private lateinit var timePickerText: TextView
    private lateinit var recurrenceSpinner: Spinner
    private lateinit var saveButton: Button
    private lateinit var spinner: Spinner
    private var containerId = 0
    private var containerName = ""
    private var repeatingInterval = 0L
    private lateinit var calendar: Calendar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_notifications)

        //todo receber containerId via intent
        containerId = intent.getIntExtra("containerId", 0)
        containerName = intent.getStringExtra("containerName").toString()
        title = "Criar notificação: "+containerName
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        messageEditText = findViewById(R.id.messageEditText)
        datePickerButton = findViewById(R.id.datePickerButton)
        timePickerButton = findViewById(R.id.timePickerButton)
        datePickerText = findViewById(R.id.dateTextView)
        timePickerText = findViewById(R.id.timeTextView)
        saveButton = findViewById(R.id.saveButton)
        recurrenceSpinner = findViewById(R.id.recurrenceSpinner)
        spinner = findViewById(R.id.notificationTypeSpinner)

        calendar = Calendar.getInstance()

        getNow()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        datePickerButton.setOnClickListener {
            showDatePicker()
        }

        timePickerButton.setOnClickListener {
            showTimePicker()
        }

        saveButton.setOnClickListener {
            saveNotification()
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedType = parent?.getItemAtPosition(position).toString()
                when (selectedType) {
                    "Aviso Recorrente" -> showRecurringNotificationViews()
                    "Prazo de Validade" -> hideRecurringNotificationViews()
                    "Aviso Simples" -> showSimpleNotificationViews()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun showRecurringNotificationViews() {
        datePickerButton.visibility = View.VISIBLE
        timePickerButton.visibility = View.VISIBLE
        recurrenceSpinner.visibility = View.VISIBLE
        messageEditText.visibility = View.VISIBLE
        datePickerText.visibility = View.VISIBLE
        timePickerText.visibility = View.VISIBLE
    }

    private fun hideRecurringNotificationViews() {
        datePickerText.visibility = View.VISIBLE
        timePickerText.visibility = View.VISIBLE
        datePickerButton.visibility = View.VISIBLE
        timePickerButton.visibility = View.VISIBLE

        messageEditText.visibility = View.GONE
        recurrenceSpinner.visibility = View.GONE
    }

    private fun showSimpleNotificationViews() {
        datePickerButton.visibility = View.VISIBLE
        timePickerButton.visibility = View.VISIBLE
        messageEditText.visibility = View.VISIBLE
        datePickerText.visibility = View.VISIBLE
        timePickerText.visibility = View.VISIBLE

        recurrenceSpinner.visibility = View.GONE
    }


    private fun showDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                datePickerText.setText(""+calendar.get(Calendar.DAY_OF_MONTH)+"/"+calendar.get(Calendar.MONTH)+"/"+calendar.get(Calendar.YEAR))
            },
            year,
            month,
            dayOfMonth
        )
        datePickerDialog.show()
    }

    private fun showTimePicker() {
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHourOfDay, selectedMinute ->
                calendar.set(Calendar.HOUR_OF_DAY, selectedHourOfDay)
                calendar.set(Calendar.MINUTE, selectedMinute)
                val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)

                val hourString = String.format("%02d", hourOfDay)
                val minuteString = String.format("%02d", minute)

                val timeString = "$hourString:$minuteString"

                timePickerText.text = timeString
            },
            hourOfDay,
            minute,
            true
        )
        timePickerDialog.show()
    }

    private fun saveNotification() {
        val notificationTime = calendar.timeInMillis
        var message = messageEditText.text.toString()

        val containerAlarmManager = ContainerAlarmManager(this)

        when (spinner.selectedItem.toString()) {
            "Aviso Recorrente" -> {
                repeatingInterval = calculateRepeatingInterval(recurrenceSpinner.selectedItem.toString())
            }
            "Prazo de Validade" -> {
                repeatingInterval = 0L
                message = "O prazo de validade para esse item termina hoje."
            }
            "Aviso Simples" -> {
                repeatingInterval = 0L
            }
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            containerAlarmManager.scheduleNotification(containerId, notificationTime, containerName, message, repeatingInterval)
        }
        setResult(1, intent)
        finish()
    }

    private fun calculateRepeatingInterval(recurrence: String): Long {
        return when (recurrence) {
            "Diária" -> 86400000
            "Semanal" -> 604800000
            "Mensal" -> 2592000000
            "Anual" -> 31536000000
            else -> 0
        }
    }

    private fun getNow()
    {
        val calendar = Calendar.getInstance()

        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val hourString = String.format("%02d", hourOfDay)
        val minuteString = String.format("%02d", minute)

        timePickerText.text = "$hourString:$minuteString"

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // O mês é baseado em zero, então adicione 1
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val monthString = String.format("%02d", month)
        val dayOfMonthString = String.format("%02d", dayOfMonth)

        datePickerText.text = "$dayOfMonthString/$monthString/$year"

    }
}
