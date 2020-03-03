package com.yqbj.ghxm.contact.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.nos.NosService;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.business.session.actions.PickImageAction;
import com.netease.yqbj.uikit.common.media.picker.PickImageHelper;
import com.yqbj.ghxm.R;
import com.yqbj.ghxm.common.ui.BaseAct;
import com.yqbj.ghxm.config.Constants;
import com.yqbj.ghxm.requestutils.api.UserApi;
import com.yqbj.ghxm.requestutils.requestCallback;
import com.yqbj.ghxm.utils.StringUtil;
import com.yuyh.easyadapter.recyclerview.EasyRVAdapter;
import com.yuyh.easyadapter.recyclerview.EasyRVHolder;

import org.json.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.netease.yqbj.uikit.business.session.constant.Extras.EXTRA_FILE_PATH;

public class AlbumActivity extends BaseAct {

    private Context context;
    private String urlData;

    private LinearLayout llNoDate;
    private ImageView imgAdd;
    private TextView tvNodata;
    private TextView tvAdd;
    private RecyclerView mRecyclerView;
    private EasyRVAdapter mAdapter;
    private List<String> urlList = new ArrayList<>();
    private String accId = "";

    public static void start(Context context, String urlData, String accId) {
        Intent intent = new Intent();
        intent.setClass(context, AlbumActivity.class);
        intent.putExtra("urlData", urlData);
        intent.putExtra("accId", accId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.albumactivity_layout);
        context = this;

        urlData = getIntent().getStringExtra("urlData");
        accId = getIntent().getStringExtra("accId");
        initView();
        initData();

        setToolbar(R.drawable.jrmf_b_top_back,"我的相片");
    }

    private void initData() {
        if (StringUtil.isNotEmpty(urlData)){
            try {
                JSONArray array = new JSONArray(urlData);
                for (int i = 0; i < array.length(); i++){
                    urlList.add((String) array.get(i));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (urlList.size() > 0){
            imgAdd.setVisibility(View.GONE);
        }else {
            imgAdd.setVisibility(View.VISIBLE);
        }
        if (urlList.size() > 1){
            tvAdd.setVisibility(View.GONE);
        } else {
            if (accId.equals(NimUIKit.getAccount())){
                tvAdd.setVisibility(View.VISIBLE);
            }else {
                tvAdd.setVisibility(View.GONE);
            }
        }
        mAdapter = new EasyRVAdapter(context,urlList,R.layout.album_item_layout) {
            @Override
            protected void onBindData(EasyRVHolder viewHolder, int position, Object item) {
                ImageView img = viewHolder.getView(R.id.img_album);
                Glide.with(context).load(urlList.get(position)).into(img);
            }
        };
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new EasyRVAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, Object item) {
                AlbumDetailActivity.start(context,position,urlList,accId);
            }
        });
    }

    private void initView() {
        llNoDate = findView(R.id.ll_nodata);
        tvNodata = findView(R.id.tv_noData_content);
        imgAdd = findView(R.id.img_add);
        tvAdd = findView(R.id.tv_add);
        tvNodata.setText("没有个人相片");
        mRecyclerView = findView(R.id.mRecyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        if (accId.equals(NimUIKit.getAccount())){
            llNoDate.setVisibility(View.GONE);
            imgAdd.setVisibility(View.VISIBLE);
            tvAdd.setVisibility(View.VISIBLE);
        }else {
            llNoDate.setVisibility(View.VISIBLE);
            imgAdd.setVisibility(View.GONE);
            tvAdd.setVisibility(View.GONE);
        }

        tvAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PickImageHelper.PickImageOption option = new PickImageHelper.PickImageOption();
                option.titleResId = R.string.set_album_image;
                option.crop = true;
                option.multiSelect = false;
                option.cropOutputImageWidth = 720;
                option.cropOutputImageHeight = 720;
                PickImageHelper.pickImage(context, 100, option);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 100){
                String path = data.getStringExtra(EXTRA_FILE_PATH);
                upDataAlbum(path);
            } else if (requestCode == 10){
                urlList = (List<String>) data.getSerializableExtra("urlList");
                if (null != urlList){
                    if (urlList.size() > 1){
                        tvAdd.setVisibility(View.GONE);
                    }else {
                        tvAdd.setVisibility(View.VISIBLE);
                    }
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    private void upDataAlbum(String path) {
        if (StringUtil.isEmpty(path)){
            return;
        }
        File file = new File(path);
        if (file == null) {
            return;
        }
        AbortableFuture<String> upload = NIMClient.getService(NosService.class).upload(file, PickImageAction.MIME_JPEG);
        upload.setCallback(new RequestCallbackWrapper<String>() {
            @Override
            public void onResult(int i, final String url, Throwable throwable) {
                if (StringUtil.isEmpty(url)){
                    return;
                }
                showProgress(context,false);
                urlList.add(url);
                Gson gson = new Gson();
                String cardUrlInfo = gson.toJson(urlList);
                UserApi.upDateUserBusinessCard(cardUrlInfo, context, new requestCallback() {
                    @Override
                    public void onSuccess(int code, Object object) {
                        dismissProgress();
                        if (code == Constants.SUCCESS_CODE){
                            if (urlList.size() > 1){
                                tvAdd.setVisibility(View.GONE);
                            }else {
                                tvAdd.setVisibility(View.VISIBLE);
                            }
                            imgAdd.setVisibility(View.GONE);
                            mAdapter.notifyDataSetChanged();
                            toast("上传成功");
                        }else {
                            toast((String) object);
                        }
                    }

                    @Override
                    public void onFailed(String errMessage) {
                        dismissProgress();
                        toast(errMessage);
                    }
                });
            }
        });
    }
}
