package cn.hayring.view.cytoscape

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import cn.hayring.view.cytoscape.databinding.ActivityMainBinding
import cn.hayring.view.cytoscapeview.bean.Node

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.addBtn.setOnClickListener {
            val node = SimpleNode(binding.idInput.text.toString(), binding.nameInput.text.toString())
            binding.cytoscapeView.addNode(node)
        }

        binding.removeBtn.setOnClickListener {
            binding.cytoscapeView.removeNode(binding.idInput.text.toString())
        }

    }
}