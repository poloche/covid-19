package com.example.demo.view

import javafx.collections.FXCollections
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import tornadofx.*
import java.io.File
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


class MainView : View("Covid-19 Follow up") {
    private val path = "series.txt"
    private var total = FXCollections.observableArrayList<XYChart.Data<String, Number>>()
    private var curados = FXCollections.observableArrayList<XYChart.Data<String, Number>>()
    private var fallecidos = FXCollections.observableArrayList<XYChart.Data<String, Number>>()
    private var activos = FXCollections.observableArrayList<XYChart.Data<String, Number>>()

    override val root = vbox {
        label(title) { }
        linechart("Avance Corona Virus", CategoryAxis(), NumberAxis()) {
            series("Total Casos", total)
            series("Curados", curados)
            series("Fallecidos", fallecidos)
            series("Activos", activos)
        }
    }

    init {
        loadFromWeb()
    }

    private fun loadFromWeb() {
        val input = File("input.html")
        if (!input.exists()) {
            input.createNewFile()
        }
        val doc = Jsoup.connect("https://www.worldometers.info/coronavirus/").get()

        if (!File(path).exists()) {
            File(path).createNewFile()
            appendHeader()
        }
        //2. Parses and scrapes the HTML response
        val date = doc.select("div.content-inner").first().child(4)
        appendText(getFormattedDate(date))
        var cases = 0
        var death = 0
        var recovered = 0
        doc.select("div#maincounter-wrap").forEachIndexed { index, element ->
            println(element.text())
            when (index) {
                0 -> cases = getNumericValue(element)
                1 -> death = getNumericValue(element)
                else -> recovered = getNumericValue(element)
            }
        }
        var actives = cases-death-recovered
        appendText("$cases,$recovered,$death,$actives"+System.getProperty("line.separator"))


        loadFromFile()
    }

    private fun loadFromFile() {
        var i = 0
        File(path).forEachLine {
            if (i != 0 && it.isNotBlank()) {

                val line = it.split(",")
                val fecha = line[0]
                val totalCasos = line[1].trim().toInt()
                val totalCurados = line[2].trim().toInt()
                val totalFallecidos = line[3].trim().toInt()
                total.add(XYChart.Data(fecha, totalCasos))
                curados.add(XYChart.Data(fecha, totalCurados))
                fallecidos.add(XYChart.Data(fecha, totalFallecidos))
                val casosActivos = totalCasos - totalCurados - totalFallecidos
                activos.add(XYChart.Data(line[0], casosActivos))
            }
            i++
        }
    }

    private fun getFormattedDate(date: Element): String {
        var dateString = date.text().split(":")[1]
        dateString = dateString.substring(0, dateString.length - 4).trim()
        val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH)
        return LocalDate.parse(dateString, formatter).toString() + ","
    }

    private fun appendHeader() {
        val text = "Date, Total, Curados, Fallecidos, Activos " + System.getProperty("line.separator")
        appendText(text)
    }

    private fun appendText(text: String) {
        try {
            File(path).appendText(text)
        } catch (e: IOException) {
            println("can't add text: $text")
        }
    }

    private fun getNumericValue(element: Element): Int {
        val textValue = element.text().split(":")[1]
        return textValue.replace(",", "").trim().toInt()
    }
}