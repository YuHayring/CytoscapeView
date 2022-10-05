package cn.hayring.view.cytoscape

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import cn.hayring.view.cytoscape.databinding.ActivityMainBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.controlPanel.setOnTouchListener { v, event ->
            v.onTouchEvent(event)
            //collect any event
            true
        }

        binding.expandSwitch.setOnCheckedChangeListener { _, isChecked ->
            BottomSheetBehavior.from(binding.controlPanel).let {
                Log.d("MainActivity", "state: ${it.state}")
                it.state = if (isChecked) {
                    BottomSheetBehavior.STATE_EXPANDED
                } else {
                    BottomSheetBehavior.STATE_COLLAPSED
                }
            }
        }

        binding.addBtn.setOnClickListener {
            val node = SimpleNode(binding.idInput.text.toString(), binding.paramInput.text.toString())
            binding.cytoscapeView.addNode(node)
        }

        binding.removeBtn.setOnClickListener {
            binding.cytoscapeView.removeElement(binding.idInput.text.toString())
        }

        binding.addEdgeBtn.setOnClickListener {
            binding.cytoscapeView.addEdge(SimpleEdge(
                binding.idInput.text.toString(),
                binding.sourceInput.text.toString(),
                binding.paramInput.text.toString()
            ))
        }

        binding.clear.setOnClickListener {
            binding.idInput.setText("")
            binding.sourceInput.setText("")
            binding.paramInput.setText("")
        }

        binding.filterNodeBtn.setOnClickListener {
            binding.cytoscapeView.filterNode(binding.paramInput.text.toString()) {
                runOnUiThread {
                    binding.logcat.text = "${binding.logcat.text}\nnode size: ${it.size}"
                }
            }

        }

        binding.filterEdgeBtn.setOnClickListener {
            binding.cytoscapeView.filterEdge(binding.paramInput.text.toString()) {
                runOnUiThread {
                    binding.logcat.text = "${binding.logcat.text}\nedge size: ${it.size}"
                }
            }

        }

    }
}