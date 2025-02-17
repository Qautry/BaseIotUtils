package com.wave_chtj.example.allapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chtj.base_framework.FIPTablesTools;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.ToastUtils;
import com.face_chtj.base_iotutils.app.AppsUtils;
import com.face_chtj.base_iotutils.entity.AppEntity;
import com.face_chtj.base_iotutils.BaseIotUtils;
import com.wave_chtj.example.R;
import com.wave_chtj.example.util.TrafficStatistics;

import java.util.List;

/**
 * Create on 2020/6/29
 * author chtj
 * desc
 */
public class AllAppAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "AllAppAdapter";

    private List<AppEntity> list;

    public AllAppAdapter(List<AppEntity> list) {
        this.list = list;
    }

    public void setList(List<AppEntity> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(BaseIotUtils.getContext()).inflate(R.layout.adapter_allapp, null, false);
        RecyclerView.ViewHolder holder = null;
        holder = new MyViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final int posiNum = position;
        MyViewHolder myViewHolder=(MyViewHolder) holder;
        myViewHolder.tvAppName.setText(list.get(position).getAppName());
        myViewHolder.tvPackName.setText(list.get(position).getPackageName());
        myViewHolder.tvUid.setText("UID:" + list.get(position).getUid() + "");
        myViewHolder.ivAppIcon.setImageDrawable(list.get(position).getIcon());
        KLog.d(TAG, " uid= " + list.get(position).getUid());
        for (int i = 0; i < list.get(position).getmProcessEntity().size(); i++) {
                Log.d(TAG, "onCreate: process pid="+list.get(position).getmProcessEntity().get(i).getPid()+",pkgName="+list.get(position).getmProcessEntity().get(i).getProcessName());

        }
        //4.4系统获取流量
        double traffic = TrafficStatistics.getUidFlow(list.get(position).getUid());
        double sumTraffic = TrafficStatistics.getDouble(traffic / 1024 / 1024);
        myViewHolder.tvTraffic.setText("use:" + sumTraffic + "M");
        myViewHolder.tvVersion.setText("v:" +list.get(position).getVersionName()+"."+list.get(position).getVersionCode());
        //7.1.2系统获取流量
        //long total= FNetworkTools.getEthAppUsageByUid(list.get(position).getUid(),FNetworkTools.getTimesMonthMorning(), FNetworkTools.getNow());
        //String totalPhrase = Formatter.formatFileSize(BaseIotUtils.getContext(), total);
        //((MyViewHolder) holder).tvTraffic.setText("流量消耗:" + totalPhrase);
        myViewHolder.tvStartApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KLog.d(TAG, "PackageName: " + list.get(posiNum).getPackageName());
                try {
                    AppsUtils.openPackage(list.get(posiNum).getPackageName());
                } catch (Exception e) {
                    e.printStackTrace();
                    KLog.e(TAG, "errMeg:" + e.getMessage());
                    ToastUtils.error("打开错误!");
                }
            }
        });
        //复制到剪切板
        myViewHolder.tvCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取剪贴板管理器：
                ClipboardManager cm = (ClipboardManager) BaseIotUtils.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                // 创建普通字符型ClipData
                ClipData mClipData = ClipData.newPlainText("Label", list.get(posiNum).getPackageName());
                // 将ClipData内容放到系统剪贴板里。
                cm.setPrimaryClip(mClipData);
                ToastUtils.success("复制包名成功");
            }
        });
        myViewHolder.tvToAppInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent localIntent = new Intent();
                localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (Build.VERSION.SDK_INT >= 9) {
                    localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                    localIntent.setData(Uri.fromParts("package", list.get(posiNum).getPackageName(), null));
                } else if (Build.VERSION.SDK_INT <= 8) {
                    localIntent.setAction(Intent.ACTION_VIEW);
                    localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
                    localIntent.putExtra("com.android.settings.ApplicationPkgName", list.get(posiNum).getPackageName());
                }
                BaseIotUtils.getContext().startActivity(localIntent);
            }
        });
        myViewHolder.tvUnInstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ToastUtils.info(Build.VERSION.SDK_INT + "  " + Build.VERSION_CODES.LOLLIPOP);
                if (AppsUtils.isRoot()) {
                    //如果已经root过 静默卸载
                    AppsUtils.uninstall(list.get(posiNum).getPackageName());
                } else {
                    //提示卸载 带弹窗
                    KLog.d(TAG, "uninstallSilent");
                    boolean isOk = AppsUtils.uninstallSilent(list.get(posiNum).getPackageName(), false);
                    if (isOk) {
                        ToastUtils.success("卸载成功");
                        list.remove(list.get(posiNum));
                        notifyDataSetChanged();
                    } else {
                        ToastUtils.error("卸载失败");
                    }
                }
            }
        });
        myViewHolder.tvEnableNet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isClearResult = FIPTablesTools.clearRule(list.get(posiNum).getUid());
                if (isClearResult) {
                    ToastUtils.success("启用成功,该应用可正常上网！");
                } else {
                    ToastUtils.error("启用成功,请重试！");
                }
            }
        });
        myViewHolder.tvDisableNet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isPutComplete = FIPTablesTools.putDisableRule(list.get(posiNum).getUid());
                if (isPutComplete) {
                    ToastUtils.success("禁用成功,该应用无法上网！");
                } else {
                    ToastUtils.error("禁用失败,请重试！");
                }
            }
        });
        if (list.get(position).getIsSys()) {
            myViewHolder.tvUnInstall.setVisibility(View.GONE);
        } else {
            myViewHolder.tvUnInstall.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvAppName, tvPackName, tvUid, tvCopy, tvToAppInfo, tvStartApp, tvTraffic, tvUnInstall, tvEnableNet, tvDisableNet,tvVersion;
        public ImageView ivAppIcon;

        public MyViewHolder(View itemView) {
            super(itemView);
            tvAppName = itemView.findViewById(R.id.tvAppName);
            tvPackName = itemView.findViewById(R.id.tvPackName);
            tvCopy = itemView.findViewById(R.id.tvCopy);
            tvToAppInfo = itemView.findViewById(R.id.tvToAppInfo);
            tvUid = itemView.findViewById(R.id.tvUid);
            ivAppIcon = itemView.findViewById(R.id.ivAppIcon);
            tvStartApp = itemView.findViewById(R.id.tvStartApp);
            tvTraffic = itemView.findViewById(R.id.tvTraffic);
            tvUnInstall = itemView.findViewById(R.id.tvUnInstall);
            tvEnableNet = itemView.findViewById(R.id.tvEnableNet);
            tvDisableNet = itemView.findViewById(R.id.tvDisableNet);
            tvVersion = itemView.findViewById(R.id.tvVersion);
        }
    }
}