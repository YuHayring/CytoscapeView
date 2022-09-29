package cn.hayring.view.cytoscape

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import cn.hayring.view.CytoscapeView
import cn.hayring.view.cytoscape.databinding.ActivityMainBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.floatingActionButton.setOnClickListener {
            binding.cytoscapeView.addTest()
        }

    }
}