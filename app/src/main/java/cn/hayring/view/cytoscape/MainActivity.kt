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

        binding.floatingActionButton.setOnClickListener {
            val node = object : Node {
                override var id: String = "1"

            }
            binding.cytoscapeView.addNode(node)
        }

    }
}