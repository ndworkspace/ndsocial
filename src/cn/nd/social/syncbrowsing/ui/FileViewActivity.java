package cn.nd.social.syncbrowsing.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cn.nd.social.R;
import cn.nd.social.util.Utils;
import cn.nd.social.util.file.FileIconHelper;
import cn.nd.social.util.file.FileInfo;
import cn.nd.social.util.file.FileSortHelper;
import cn.nd.social.util.file.FilenameExtFilter;

public class FileViewActivity extends Activity {

	private ListView mFileListView;
	private TextView mNavigationBarText;	
	
	private ArrayList<FileInfo> mFileNameList = new ArrayList<FileInfo>();
	String []EXT_FILTER = {"pdf"};
	private FilenameExtFilter mExtFilter;
	
	private ArrayAdapter<FileInfo> mAdapter;
	private FileSortHelper mSortHelper;
	private FileIconHelper mIconHelper;
	
	private String ROOT_PATH = "/";
	private String mSdDir = Utils.getSdDirectory();
	private String mCurrentPath;
	
	private final static String TAG = "FileViewActivity";
	
	
	public final static int MODE_PICK = 0;
	private int mCurrentMode = MODE_PICK;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sync_file_explorer_list);
		mExtFilter = new FilenameExtFilter(EXT_FILTER);
		mSortHelper = new FileSortHelper();
		mIconHelper = new FileIconHelper();
		mCurrentPath = new File(mSdDir).getParent();
		setupViews();
	}
	
	private  void setupViews() {

		setupListView();
		setupNaivgationBar();
		TextView title = (TextView)findViewById(R.id.title);
		title.setText(R.string.select_file);
		
		mAdapter = new FileListAdapter(this, R.layout.sync_file_browser_item, mFileNameList,mIconHelper);
		mFileListView.setAdapter(mAdapter);
		
		updateUI();
	}
	
	private void setupListView() {
		mFileListView = (ListView) findViewById(R.id.file_path_list);
		
        mFileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	FileInfo lFileInfo = mFileNameList.get(position);
                if (lFileInfo == null) {
                    Log.e(TAG, "file does not exist on position:" + position);
                    return;
                }

                if (!lFileInfo.IsDir) {
                    if (mCurrentMode == MODE_PICK) {
                    	returnTarget(lFileInfo);
                    } else {
                    	//TODO: view file
                        //viewFile(lFileInfo);
                    }
                    return;
                }

                mCurrentPath = getAbsoluteName(mCurrentPath, lFileInfo.fileName);
                refreshFileList();
            }
        });
	}
    

    
    private void setupNaivgationBar() {
        mNavigationBarText = (TextView) findViewById(R.id.current_path_view);
        View upLevelBtn = findViewById(R.id.path_pane_up_level);
        upLevelBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
		        if (!ROOT_PATH.equals(mCurrentPath)) {
		            mCurrentPath = new File(mCurrentPath).getParent();
		            refreshFileList();
		        }
				
			}
		});
    }
	
    private void showEmptyView(boolean show) {
        View emptyView = findViewById(R.id.empty_view);
        if (emptyView != null)
            emptyView.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    
    public boolean onRefreshFileList(String path, FileSortHelper sort) {
        File file = new File(path);
        if (!file.exists() || !file.isDirectory()) {
            return false;
        }
        //tangtaotao@ND_20140220 if directory is not readable, do not refresh the file list
        if(!file.canRead()) {
        	mCurrentPath = new File(mCurrentPath).getParent();
        	Toast.makeText(Utils.getAppContext(), R.string.system_file_not_readable, Toast.LENGTH_SHORT).show();
        	return false;
        }

        ArrayList<FileInfo> fileList = mFileNameList;
        fileList.clear();

        File[] listFiles = file.listFiles(mExtFilter);
        if (listFiles == null)
            return true;

        for (File child : listFiles) {
            String absolutePath = child.getAbsolutePath();
            if (Utils.shouldShowFile(absolutePath)) {
                FileInfo lFileInfo = Utils.GetFileInfo(child,
                		mExtFilter, false);
                if (lFileInfo != null) {
                    fileList.add(lFileInfo);
                }
            }
        }

        Collections.sort(mFileNameList, sort.getComparator());
        showEmptyView(fileList.size() == 0);
        mAdapter.notifyDataSetChanged();
        return true;
    }
    
    private void updateUI() {
        boolean sdCardReady = Utils.isSDCardReady();
        View noSdView = findViewById(R.id.sd_not_available_page);
        noSdView.setVisibility(sdCardReady ? View.GONE : View.VISIBLE);

        View navigationBar = findViewById(R.id.navigation_bar);
        navigationBar.setVisibility(sdCardReady ? View.VISIBLE : View.GONE);
        mFileListView.setVisibility(sdCardReady ? View.VISIBLE : View.GONE);

        if(sdCardReady) {
        	refreshFileList();
        }
    }
    
    private void refreshFileList() {
    	if(onRefreshFileList(mCurrentPath, mSortHelper)) {
    		updateNavigationPane();
    	}
        
    }

    private void updateNavigationPane() {
        View upLevel = findViewById(R.id.path_pane_up_level);
        upLevel.setVisibility(ROOT_PATH.equals(mCurrentPath) ? View.INVISIBLE : View.VISIBLE);
        mNavigationBarText.setText(getDisplayPath(mCurrentPath));
    }
    
    public String getDisplayPath(String path) {
        if (path.startsWith(mSdDir)) {
            return getString(R.string.sd_folder) + path.substring(mSdDir.length());
        } else {
            return path;
        }
    }

	
    private String getAbsoluteName(String path, String name) {
        return path.equals(ROOT_PATH) ? path + name : path + File.separator + name;
    }
    
	private void returnTarget(FileInfo fileInfo) {
		Intent returnIntent = new Intent();
		returnIntent.putExtra(HostSyncActivity.FILE_ID_KEY, fileInfo.filePath);
		setResult(RESULT_OK, returnIntent);
		finish();
	}
}
