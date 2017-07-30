package com.example.enzo.bluetooth;


import android.app.Activity;
import android.app.Fragment;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {
    private ScrollView                       mScrollView;
    private LinearLayout                     mLayoutContainer;
    private OnSoftKeyboardVisibilityListener mSoftKeyboardVisibilityListener;

    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mScrollView = view.findViewById(R.id.scroll_view);
        mLayoutContainer = view.findViewById(R.id.layout_container);
        mSoftKeyboardVisibilityListener = new OnSoftKeyboardVisibilityListener(getActivity(), mScrollView) {
            @Override
            public void onKeyboardVisibilityChanged(boolean isVisible) {
                scrollViewToBottom();
            }
        };
        mScrollView.getViewTreeObserver().addOnGlobalLayoutListener(mSoftKeyboardVisibilityListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mScrollView.getViewTreeObserver().removeOnGlobalLayoutListener(mSoftKeyboardVisibilityListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private LinearLayout createChatBubble() {
        return (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.chat_bubble, mLayoutContainer, false);
    }

    public void scrollViewToBottom() {
        mScrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScrollView.fullScroll(View.FOCUS_DOWN);
            }
        }, 100);
    }

    public void onMessageSent(String name, String message) {
        LinearLayout chatBubble = createChatBubble();
        chatBubble.setGravity(Gravity.END);
        TextView textName = chatBubble.findViewById(R.id.text_name);
        TextView textContent = chatBubble.findViewById(R.id.text_content);
        textContent.setBackgroundResource(R.drawable.shape_ripple_round_rect_green);

        textName.setText(name);
        textContent.setText(message);
        mLayoutContainer.addView(chatBubble);

        scrollViewToBottom();
    }

    public void onMessageReceived(String name, String message) {
        LinearLayout chatBubble = createChatBubble();
        chatBubble.setGravity(Gravity.START);
        TextView textName = chatBubble.findViewById(R.id.text_name);
        TextView textContent = chatBubble.findViewById(R.id.text_content);
        textContent.setBackgroundResource(R.drawable.shape_ripple_round_rect_blue);

        textName.setText(name);
        textContent.setText(message);
        mLayoutContainer.addView(chatBubble);

        scrollViewToBottom();
    }

    public static abstract class OnSoftKeyboardVisibilityListener implements OnGlobalLayoutListener {
        private Activity mContextActivity;
        private View     mOutermostLayout;
        private Boolean  mKeyboardVisibility;

        public OnSoftKeyboardVisibilityListener(Activity contextActivity, View outermostLayout) {
            mContextActivity = contextActivity;
            mOutermostLayout = outermostLayout;
        }

        @Override
        public void onGlobalLayout() {
            Rect rect = new Rect();
            mContextActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
            int screenHeight = mOutermostLayout.getRootView().getHeight();
            int heightDiff = screenHeight - (rect.bottom - rect.top);
            int statusBarHeight = getStatusBarHeight();
            int navBarHeight = getNavigationBarHeight();

            boolean isKeyboardVisible = (heightDiff - statusBarHeight < screenHeight / 5);
            if (mKeyboardVisibility == null || mKeyboardVisibility != isKeyboardVisible) {
                onKeyboardVisibilityChanged(isKeyboardVisible);
            }
            mKeyboardVisibility = isKeyboardVisible;
        }


        public int getStatusBarHeight() {
            Resources resources = mContextActivity.getResources();
            int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                return resources.getDimensionPixelSize(resourceId);
            }
            return 0;
        }

        public int getNavigationBarHeight() {
            Resources resources = mContextActivity.getResources();
            int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                return resources.getDimensionPixelSize(resourceId);
            }
            return 0;
        }

        abstract public void onKeyboardVisibilityChanged(boolean isVisible);
    }
}
