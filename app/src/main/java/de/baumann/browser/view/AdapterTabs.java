package de.baumann.browser.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.card.MaterialCardView;

import de.baumann.browser.R;
import de.baumann.browser.browser.AlbumController;
import de.baumann.browser.browser.BrowserContainer;
import de.baumann.browser.browser.BrowserController;

class AdapterTabs {

    private final Context context;
    private final AlbumController albumController;

    private View albumView;
    private TextView albumTitle;
    private BrowserController browserController;
    private LinearLayout albumCardView;
    private DrawerLayout drawerLayout;

    AdapterTabs(Context context, AlbumController albumController, BrowserController browserController,DrawerLayout drawerLayout) {
        this.context = context;
        this.albumController = albumController;
        this.browserController = browserController;
        this.drawerLayout=drawerLayout;
        initUI();
    }

    View getAlbumView() {
        return albumView;
    }

    void setAlbumTitle(String title) {
        albumTitle.setText(title);
    }

    void setBrowserController(BrowserController browserController) {
        this.browserController = browserController;
    }

    @SuppressLint("InflateParams")
    private void initUI() {
        albumView = LayoutInflater.from(context).inflate(R.layout.item_list, null, false);
        albumCardView = albumView.findViewById(R.id.albumCardView);
        albumTitle = albumView.findViewById(R.id.titleView);

        ImageButton albumClose = albumView.findViewById(R.id.cancelButton);
        albumClose.setVisibility(View.VISIBLE);
        albumClose.setOnClickListener(view -> {
            browserController.removeAlbum(albumController);
            if (BrowserContainer.size() < 2) {
                browserController.hideOverview();
            }
        });
    }
//点击某一网页
    public void activate() {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorSecondaryContainer, typedValue, true);
        int color = typedValue.data;
        albumCardView.setBackgroundColor(color);
        albumTitle.setOnClickListener(view -> {
            albumCardView.setBackgroundColor(color);
            browserController.hideOverview();
            drawerLayout.closeDrawer(GravityCompat.START);
        });
    }
//使某一网页颜色失效
    void deactivate() {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorSurfaceVariant, typedValue, true);
        int color = typedValue.data;
        albumCardView.setBackgroundColor(color);
        albumTitle.setOnClickListener(view -> {
            browserController.showAlbum(albumController);
            browserController.hideOverview();
            drawerLayout.closeDrawer(GravityCompat.START);
        });
    }
}