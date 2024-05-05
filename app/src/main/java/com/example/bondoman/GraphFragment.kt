
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.bondoman.R
import com.example.bondoman.data.Transaction
import com.example.bondoman.data.TransactionViewModel
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

class GraphFragment : Fragment() {

    private lateinit var pieChart: PieChart
    private lateinit var transactionViewModel: TransactionViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_graph, container, false)

        pieChart = rootView.findViewById(R.id.pieChart)

        transactionViewModel = ViewModelProvider(this).get(TransactionViewModel::class.java)

        // Observe the transaction data
        transactionViewModel.getAllTransactions.observe(viewLifecycleOwner, Observer { transactions ->
            val totalIncome = calculateTotal(transactions, "Pemasukan")
            val totalExpenses = calculateTotal(transactions, "Pengeluaran")
            val total = totalIncome + totalExpenses
            val incomePercentage = (totalIncome / total) * 100
            val expensesPercentage = (totalExpenses / total) * 100

            val entries = ArrayList<PieEntry>()
            entries.add(PieEntry(totalIncome.toFloat(), "Pemasukan (${String.format("%.2f", incomePercentage)}%)"))
            entries.add(PieEntry(totalExpenses.toFloat(), "Pengeluaran (${String.format("%.2f", expensesPercentage)}%)"))

            val dataSet = PieDataSet(entries, "Transaction Summary")
            dataSet.colors = listOf(Color.parseColor("#88BFC3"), Color.parseColor("#E9A3A3"))
            dataSet.valueTextSize = 20f
            dataSet.valueTextColor = Color.BLACK
            pieChart.holeRadius = 0f
            pieChart.legend.textSize = 16f
            pieChart.legend.isWordWrapEnabled = true
            setLegendPosition(pieChart.resources.configuration.orientation)
            pieChart.legend.orientation = Legend.LegendOrientation.VERTICAL
            val pieData = PieData(dataSet)
            pieChart.data = pieData

            pieChart.description.isEnabled = false
            pieChart.transparentCircleRadius = 0f
            pieChart.invalidate()
        })

        return rootView
    }

    private fun calculateTotal(transactions: List<Transaction>, category: String): Double {
        var total = 0.0
        for (transaction in transactions) {
            if (transaction.category == category) {
                total += transaction.price
            }
        }
        return total
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setLegendPosition(newConfig.orientation)
    }

    private fun setLegendPosition(orientation: Int) {
        val legend = pieChart.legend

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            legend.verticalAlignment = Legend.LegendVerticalAlignment.CENTER
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
            legend.xOffset = 32f
            legend.yOffset = 0f
        } else {
            legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            legend.xOffset = 0f
            legend.yOffset = 32f
        }
        pieChart.invalidate() // Refresh chart to apply changes
    }
}
