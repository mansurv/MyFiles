package com.netmontools.myfiles

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.netmontools.myfiles.databinding.ActivityMainBinding
import com.netmontools.myfiles.utils.MimeTypes
import com.netmontools.myfiles.utils.PermissionUtils
import com.netmontools.myfiles.utils.SimpleUtils
import java.io.File
import kotlin.math.max

class MainActivity : AppCompatActivity() {
    private val appBarConfiguration: AppBarConfiguration? = null
    private var binding: ActivityMainBinding? = null
    //lateinit var layoutManager: AutoFitGridLayoutManager
    private lateinit var localRefreshLayout: SwipeRefreshLayout
    lateinit var localViewModel: LocalViewModel
    lateinit var adapter: LocalAdapter

    var isSelected: Boolean = false
    var position: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        binding!!.localRefreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_bright, android.R.color.holo_green_light,
            android.R.color.holo_orange_light, android.R.color.holo_red_light
        )
        binding!!.localRefreshLayout.isEnabled = false
        val layoutManager: RecyclerView.LayoutManager = AutoFitGridLayoutManager(this, 400)
        val recyclerView = findViewById<RecyclerView>(R.id.localRecyclerView)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = layoutManager

        adapter = LocalAdapter()
        recyclerView.adapter = adapter

        localViewModel = ViewModelProvider.NewInstanceFactory().create(LocalViewModel::class.java)
        localViewModel.allPoints.observe(this, Observer<List<Folder>>
        {points -> adapter.setPoints(points)
            binding!!.localRefreshLayout.isRefreshing = false })
    }

    override fun onStart() {
        super.onStart()

        adapter.setOnItemClickListener { point ->
            isSelected = false;
            if(!point.isFile) {
                binding!!.localRefreshLayout.setRefreshing(true)
                localViewModel.update(point)
                //mainViewModel.updateActionBarTitle(point.getNameItem())
            } else {
                val file = File(point.getPathItem())
                if (file.exists() && (file.isFile())) {
                    val ext = SimpleUtils.getExtension(file.name)
                    val type = MimeTypes.getMimeType(file)
                    if (ext.equals("jpg") || (ext.equals("jpeg") || (ext.equals("bmp")))) {
//                        val navController: NavController =
//                            Navigation.findNavController(this,
//                                R.id.nav_host_fragment_activity_main);
//
//                        val bundle : Bundle = Bundle()
//                        bundle.putString("arg", file.path.toString())
//                        navController
//                            .navigate(R.id.action_navigation_home_to_navigation_image, bundle)

                        val intent: Intent = Intent(
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
                            val typ = MimeTypes.getMimeType(file)
                            intent.setAction(Intent.ACTION_VIEW)

                            intent.setDataAndType(file.path.toString().toUri(), typ )
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            //requireContext().startActivity(intent)
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

        adapter.setOnItemLongClickListener { point: Folder ->

            point.isChecked = !point.isChecked
            adapter.notifyItemChanged(position);
        }
    }


//    private inner class ImageGalleryAdapter(
//        private val mContext: Context,
//        private var mFolders: Array<Folder>
//    ) :
//        RecyclerView.Adapter<MyViewHolder>() {
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
//            val context = parent.context
//            val inflater = LayoutInflater.from(context)
//            val photoView = inflater.inflate(R.layout.local_item, parent, false)
//            val viewHolder = MyViewHolder(photoView)
//            return viewHolder
//        }
//
//        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
//            val folder = mFolders[position]
//            val imageView = holder.mPhotoImageView
//            val file = File(folder.url)
//            if (file.exists() && file.isFile) {
//                Glide
//                    .with(holder.mPhotoImageView.context)
//                    .load(file)
//                    .centerCrop()
//                    .diskCacheStrategy(DiskCacheStrategy.NONE)
//                    .into(imageView)
//            } else {
//                if (file.isDirectory) {
//                    imageView.setImageResource(R.drawable.baseline_folder_yellow_24)
//                }
//                if (file.isFile) {
//                    imageView.setImageResource(R.drawable.baseline_description_yellow_24)
//                    holder.textViewSize.setText(file.length().toInt())
//                }
//            }
//
//            holder.textViewTitle.text = folder.title
//        }
//
////        @SuppressLint("NotifyDataSetChanged")
////        fun setPoints(points: List<Folder?>) {
////            points.also { this.mFolders = it }
////            notifyDataSetChanged()
////        }
//
//        override fun getItemCount(): Int {
//            return (mFolders.size)
//        }
//
//        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
//            View.OnClickListener {
//            var textViewTitle: TextView =
//                itemView.findViewById(R.id.text_view_title)
//            var textViewSize: TextView =
//                itemView.findViewById(R.id.text_view_size)
//            var mPhotoImageView: ImageView =
//                itemView.findViewById(R.id.local_image_view)
//
//            init {
//                itemView.setOnClickListener(this)
//            }
//
//            override fun onClick(view: View) {
//                val position = adapterPosition
//                if (position != RecyclerView.NO_POSITION) {
//                    val folder = mFolders[position]
//                    try {
//                        val file = File(folder.url)
//                        if (file.exists()) {
//                            if (file.isDirectory) {
//                                App.folders.clear()
//                                try {
//
//
//                                    for (it in (file.listFiles())!!) {
//                                        if (it.exists()) {
//                                            var fd = Folder("", "")
//                                            fd.title = it.name
//                                            fd.url = it.path
//
//                                            App.folders.add(fd)
//                                            var FolderSize = App.folders.size
//                                            FolderSize++
//                                        }
//                                    }
//                                } catch (e: AccessDeniedException) {
//                                    val str = e.message.toString()
//                                    var len = str.length
//                                }
//
//                            }
//                        }
//                    } catch(e: NullPointerException) {
//                        val str = e.message.toString()
//                        var len = str.length
//                    }
////                    val intent = Intent(
////                        mContext,
////                        ImageActivity::class.java
////                    )
////                    intent.putExtra(ImageActivity.EXTRA_SPACE_PHOTO, folder);
////                    startActivity(intent);
//                }
//            }
//        }
//    }

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
    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == MainActivity.PERMISSION_STORAGE) {
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
        if (requestCode == MainActivity.PERMISSION_STORAGE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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