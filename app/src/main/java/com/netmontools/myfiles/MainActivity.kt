package com.netmontools.myfiles

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.netmontools.myfiles.databinding.ActivityMainBinding
import com.netmontools.myfiles.utils.MimeTypes
import com.netmontools.myfiles.utils.PermissionUtils
import com.netmontools.myfiles.utils.SimpleUtils
import java.io.File
import java.util.Objects
import kotlin.math.max


class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null
    private lateinit var layoutManager: AutoFitGridLayoutManager
    private lateinit var localRefreshLayout: SwipeRefreshLayout
    private val sp = PreferenceManager.getDefaultSharedPreferences(App.instance)
    private lateinit var localViewModel: LocalViewModel
    private lateinit var adapter: LocalAdapter

    private var isSelected = false
    private var isListMode: Boolean = false
    private var isBigMode: Boolean = false
    private var position: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        binding!!.localRefreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_bright, android.R.color.holo_green_light,
            android.R.color.holo_orange_light, android.R.color.holo_red_light
        )
        binding!!.localRefreshLayout.isEnabled = false
        layoutManager = AutoFitGridLayoutManager(this, 400)
        //val recyclerView = findViewById<RecyclerView>(R.id.localRecyclerView)
        binding!!.localRecyclerView.setHasFixedSize(true)
        binding!!.localRecyclerView.layoutManager = layoutManager

        adapter = LocalAdapter()
        binding!!.localRecyclerView.adapter = adapter

        if (savedInstanceState != null) {
            val mode = savedInstanceState.getInt("mode")
            if (mode == 0) {
                isListMode = false
            } else {
                isListMode = true
            }
        }

        localViewModel = ViewModelProvider.NewInstanceFactory().create(LocalViewModel::class.java)
        localViewModel.allPoints.observe(this) { points ->
            adapter.setPoints(points)
            binding!!.localRefreshLayout.isRefreshing = false
        }
    }

    override fun onStart() {
        super.onStart()

        adapter.setOnItemClickListener { point ->
            isSelected = false
            if(!point.isFile) {
                binding!!.localRefreshLayout.setRefreshing(true)
                localViewModel.update(point)
                //mainViewModel.updateActionBarTitle(point.getNameItem())
            } else {
                val file = File(point.getPathItem())
                if (file.exists() && (file.isFile())) {
                    val ext = SimpleUtils.getExtension(file.name)
                    val mime = MimeTypes.getMimeType(file)
                    if (ext.equals("jpg") || (ext.equals("jpeg") || (ext.equals("bmp")))) {

                        val intent = Intent(
                           this,
                            ImageActivity::class.java
                        )
                        intent.putExtra(ImageActivity.EXTRA_IMAGE, file.path)
                        startActivity(intent)
                    } else if (ext.equals("fb2")) {

                        val viewIntent = Intent(Intent.ACTION_VIEW)
                        viewIntent.setDataAndType(Uri.parse(file.path.toString()), "*/*")
                        val chooserIntent = Intent.createChooser(viewIntent, "Open with...")
                        startActivity(chooserIntent)

                    } else {
                        //open the file
                        try {
                            val intent = Intent()
                            intent.setAction(Intent.ACTION_VIEW)

                            intent.setDataAndType(file.path.toString().toUri(), mime )
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            this.startActivity(intent)
                        } catch (e: IllegalArgumentException) {
                            Toast.makeText(
                                //requireContext(),
                                this,
                                "Cannot open the file" + e.message.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }//try
                    }//else
                }//if
            }//else
        }//adapter

//        adapter.setOnItemLongClickListener { point: Folder ->
//            val view: View = binding!!.root
//            //point.isChecked = !point.isChecked
//            //adapter.notifyItemChanged(position);
//            val popupMenu = PopupMenu(this, view)
//            popupMenu.menu.add("SCAN")
//            popupMenu.menu.add("DELETE")
//            popupMenu.menu.add("MOVE")
//            popupMenu.menu.add("RENAME")
//
//            popupMenu.setOnMenuItemClickListener { item ->
//                if (item.title == "SCAN") {
//                    this.localViewModel.scan(point)
//                }
//                if (item.title == "DELETE") {
//                    val deleted = true //selectedFile.delete();
//                    if (deleted) {
//                        Toast.makeText(
//                            App.instance.applicationContext,
//                            "DELETED ",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                        view.setVisibility(View.GONE)
//                    }
//                }
//
//                if (item.title == "MOVE") {
//                    Toast.makeText(
//                        App.instance.applicationContext,
//                        "MOVED ",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//
//                if (item.title == "RENAME") {
//                    Toast.makeText(
//                        App.instance.applicationContext,
//                        "RENAME ",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//                true
//            }
//        }


    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.main_menu, menu)
//        return super.onCreateOptionsMenu(menu)
//    }
//
//    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
//        if (!isListMode) {
//            menu.findItem(R.id.listMode).setIcon(R.drawable.baseline_view_list_yellow_24)
//        } else {
//            menu.findItem(R.id.listMode).setIcon(R.drawable.baseline_view_column_yellow_24)
//        }
//
//        return super.onPrepareOptionsMenu(menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//
//        return when (item.itemId) {
//            R.id.property -> {
//                if (item.isChecked) {
//
//                    item.setChecked(false)
//                } else {
//                    item.setChecked(true)
//
//                }
//
//                true
//            }
//
//            R.id.listMode -> {
//                if (isListMode == false) {
//                    isListMode = true
//                    //recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
//                    binding!!.localRecyclerView.setLayoutManager(layoutManager)
//                    item.setIcon(R.drawable.baseline_view_list_yellow_24)
//                } else {
//                    isListMode = false
//                    binding!!.localRecyclerView.setLayoutManager(GridLayoutManager(this, 2))
//                    item.setIcon(R.drawable.baseline_view_column_yellow_24)
//                }
//                true
//            }
//
//            R.id.bigMode -> {
//                if (isBigMode) {
//
//                }
//
//                true
//            }
//
//            else -> return true
//        }
//        //return super.onOptionsItemSelected(item)
//    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mode = 0
        if(isListMode) {
            mode = 1
        }

        outState.putInt("mode", mode)
        outState.putInt("position", position)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        position = savedInstanceState.getInt("position")
        localViewModel.update(adapter.getPointAt(position))
    }

    override fun onPause() {
        super.onPause()
        //sp.edit().putString("actionbar_title", appBar.getTitle().toString()).apply()
        sp.edit().putBoolean("layout_mode", isListMode).apply()
    }

    override fun onResume() {
        super.onResume()
//        val actionBarTitle = sp.getString("actionbar_title", "")
//        if(actionBarTitle.equals("0")) {
//            mainViewModel.updateActionBarTitle(App.rootPath!!)
//        } else mainViewModel.updateActionBarTitle(actionBarTitle!!)

//        if (isListMode == false) {
//            binding!!.localRecyclerView.setLayoutManager(GridLayoutManager(this, 2))
//            //binding.localRecyclerView.setLayoutManager(layoutManager);
//            //menuItem.setIcon(R.drawable.baseline_view_list_yellow_24);
//        } else {
//            binding!!.localRecyclerView.setLayoutManager(LinearLayoutManager(this))
//            //menuI.setIcon(R.drawable.baseline_view_column_yellow_24);
//        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            try {
                var file: File? = null
                if (App.previousPath != null) {
                    file =  File(App.previousPath)
                }
                if (!file!!.getPath().equals(App.rootPath)) {
                    if (file.exists()) {
                        file = File(Objects.requireNonNull(file.getParent()))
                        val fd = Folder()
                        fd.isFile = file.isFile()
                        fd.setNameItem(file.getName())
                        fd.setPathItem(file.getPath())
                        if (fd.isFile) {
                            fd.setItemSize(file.length())
                            //fd.setImageItem(App.file_image)
                        } else {
                            fd.setItemSize(0L)
                            //fd.setImageItem(App.folder_image)
                        }
                        localViewModel.update(fd)
                        binding!!.localRefreshLayout.isRefreshing = true
                        //mainViewModel.updateActionBarTitle(file.getName())
                    }
                } else {
                    finish()
                }

            } catch (npe: NullPointerException) {
                npe.printStackTrace()
            }
            return true
        }

        return super.onKeyDown(keyCode, event)
    }

    inner class AutoFitGridLayoutManager(context: Context?, columnWidth: Int) :
        GridLayoutManager(context, 1) {
        private var columnWidth = 0
        private var columnWidthChanged = true

        init {
            setColumnWidth(columnWidth)
        }

        fun setColumnWidth(newColumnWidth: Int) {
            if (newColumnWidth > 0 && newColumnWidth != columnWidth) {
                columnWidth = newColumnWidth
                columnWidthChanged = true
            }
        }

        override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
            if (columnWidthChanged && columnWidth > 0) {
                val totalSpace = if (orientation == VERTICAL) {
                    width - paddingRight - paddingLeft
                } else {
                    height - paddingTop - paddingBottom
                }
                val spanCount =
                    max(1.0, (totalSpace / columnWidth).toDouble()).toInt()
                setSpanCount(spanCount)
                columnWidthChanged = true
            }
            super.onLayoutChildren(recycler, state)
        }
    }
    @Deprecated("This method has been deprecated in favor of using the Activity Result API" +
            "\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt" +
            "\n      contracts for common intents available in" +
            "\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for" +
            "\n      testing, and allow receiving results in separate, testable classes independent from your" +
            "\n      activity. Use" +
            "\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}" +
            "\n      with the appropriate {@link ActivityResultContract} and handling the result in the" +
            "\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PERMISSION_STORAGE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (PermissionUtils.hasPermissions(this)) {
                    Toast.makeText(
                        this,
                        "Permission granted",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "Permission not granted",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    this,
                    "Permission granted",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "Permission not granted",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {
        private const val PERMISSION_STORAGE = 101
    }
}