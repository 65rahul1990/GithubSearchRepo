package com.example.githubsearchrepo.fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.ArrayMap;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.allattentionhere.fabulousfilter.AAH_FabulousFragment;
import com.example.githubsearchrepo.events.FilterUpdateEvent;
import com.example.githubsearchrepo.R;
import com.example.githubsearchrepo.Utilities;
import com.example.githubsearchrepo.adapter.RepoListAdapter;
import com.example.githubsearchrepo.database.DBManager;
import com.example.githubsearchrepo.database.GithubItemEntity;
import com.example.githubsearchrepo.database.GithubMainEntity;
import com.example.githubsearchrepo.events.RefreshListEvent;
import com.example.githubsearchrepo.network.ApiManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchRepoFragment extends Fragment implements SearchView.OnQueryTextListener{
    @BindView(R.id.recyclerView) RecyclerView recyclerView;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.search) SearchView searchView;
    @BindView(R.id.feed_loader) ContentLoadingProgressBar loader;
    @BindView(R.id.tv_error_message) TextView tvErrorMessage;
    @BindView(R.id.fab) FloatingActionButton fab;
    RepoListAdapter repoListAdapter;
    Realm realm;
    RealmResults<GithubItemEntity> githubItemEntities;
    private GithubMainEntity githubMainEntity;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_repo_fragment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        realm = Realm.getDefaultInstance();

        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            toolbar.setTitle(R.string.search);
        }

        githubMainEntity = realm.where(GithubMainEntity.class).findFirst();
        if(githubMainEntity != null) {
            tvErrorMessage.setVisibility(View.GONE);
            loader.hide();
            githubItemEntities = githubMainEntity.getItems().where().findAll().sort("watchers_count", Sort.DESCENDING);;
        }


        fetchRepos("git");
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        repoListAdapter = new RepoListAdapter(getActivity(), githubItemEntities);
        recyclerView.setAdapter(repoListAdapter);


        searchView.setOnQueryTextListener(this);
        searchView.setFocusable(false);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_search).setVisible(false);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_search:
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        fetchRepos(query);
        hideKeyboard(getActivity(), getView());
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        if(s.isEmpty() && githubItemEntities.size() < 10){
            fetchRepos("git");
        }
        return true;
    }

    private void fetchRepos(String query){
        loader.show();
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("q", query);
        queryMap.put("sort", "watcher_count");
        queryMap.put("order", "desc");
        queryMap.put("per_page", 10);

        Call<ResponseBody> request = ApiManager.getInstance().searchRequest(queryMap);
        request.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()){
                    loader.hide();
                    DBManager.getInstance().createRepos(response.body());
                    updateAdapter();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                loader.hide();
            }
        });
    }

    private void updateAdapter(){
        githubMainEntity = realm.where(GithubMainEntity.class).findFirst();
        if(githubMainEntity != null) {
            githubItemEntities = githubMainEntity.getItems().where().findAll().sort("watchers_count", Sort.DESCENDING);;
        }
        if(githubItemEntities.isEmpty()){
            tvErrorMessage.setVisibility(View.VISIBLE);
        }else {
            tvErrorMessage.setVisibility(View.GONE);

        }

        repoListAdapter.updateAdapter(githubItemEntities);
    }

    @Subscribe
    public void updateAfterFilter(FilterUpdateEvent filterUpdateEvent){
        repoListAdapter.updateAdapter(filterUpdateEvent.getGithubItemEntities());

    }

    @Subscribe
    public void refreshList(RefreshListEvent refreshListEvent){
        fetchRepos("git");
    }



    @OnClick(R.id.fab)
    void filterFragment(){
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        FilterFragment newFragment = FilterFragment.newInstance();
        newFragment.setParentFab(fab);
        newFragment.show(ft, "dialog");
    }

    public  static class FilterFragment extends AAH_FabulousFragment implements  CompoundButton.OnCheckedChangeListener{
        ArrayMap<String, List<String>> applied_filters = new ArrayMap<>();
        @BindView(R.id.star) RadioButton rbtnStar;
        @BindView(R.id.fork) RadioButton rbtnFork;
        @BindView(R.id.updated) RadioButton rbtnUpdated;
        @BindView(R.id.desc) RadioButton rbtnDesc;
        @BindView(R.id.asc) RadioButton rbtnAsc;
        @BindView(R.id.llFromDate) LinearLayout llFromDate;
        @BindView(R.id.llToDate) LinearLayout llToDate;
        @BindView(R.id.tvStartDate) TextView tvStartDate;
        @BindView(R.id.tvEndDate) TextView tvEndDate;
        @BindView(R.id.rl_content) RelativeLayout rl_content;
        @BindView(R.id.ll_buttons) LinearLayout ll_buttons;

        private String sortBy;
        private String orderBy;
        RealmResults<GithubItemEntity> githubItemEntities;
        Realm realm;
        private GithubMainEntity githubMainEntity;



        private Calendar dateRangeCalendar = Calendar.getInstance();
        private DatePickerDialog.OnDateSetListener dateSetListener = null;

        public static FilterFragment newInstance() {
            return new FilterFragment();
        }

        @Override
        public void setupDialog(Dialog dialog, int style) {
            View contentView = View.inflate(getContext(), R.layout.filter_fragment, null);
            ButterKnife.bind(this, contentView);
            realm = Realm.getDefaultInstance();

            rbtnStar.setOnCheckedChangeListener(this);
            rbtnFork.setOnCheckedChangeListener(this);
            rbtnUpdated.setOnCheckedChangeListener(this);
            rbtnDesc.setOnCheckedChangeListener(this);
            rbtnAsc.setOnCheckedChangeListener(this);

            dateSetListener = (datePicker, year, monthOfYear, dayOfMonth) -> {
                dateRangeCalendar.set(Calendar.YEAR, year);
                dateRangeCalendar.set(Calendar.MONTH, monthOfYear);
                dateRangeCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel(isStartDate);
            };

            githubMainEntity = realm.where(GithubMainEntity.class).findFirst();
            if(githubMainEntity != null) {
                githubItemEntities = githubMainEntity.getItems().where().findAll().sort("watchers_count", Sort.DESCENDING);;
            }

            //params to set
            setAnimationDuration(600); //optional; default 500ms
            setPeekHeight(400); // optional; default 400dp
            setViewgroupStatic(ll_buttons); // optional; layout to stick at bottom on slide
            setViewMain(rl_content); //necessary; main bottomsheet view
            setMainContentView(contentView); // necessary; call at end before super
            super.setupDialog(dialog, style); //call super at last

        }



        private Date startDate;
        private Date endDate;
        private boolean isStartDate;
        @OnClick(R.id.llFromDate)
        void pickStartDate(){
            isStartDate = true;
            datePicker();
        }

        @OnClick(R.id.llToDate)
        void pickEndDate(){
            isStartDate = false;
            datePicker();
        }


        private void datePicker(){
            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), dateSetListener, dateRangeCalendar
                    .get(Calendar.YEAR), dateRangeCalendar.get(Calendar.MONTH),
                    dateRangeCalendar.get(Calendar.DAY_OF_MONTH));

            //date can't be later than today
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.set(Calendar.HOUR, 11);
            calendar.set(Calendar.MINUTE, 59);
            DatePicker datePickerView = datePickerDialog.getDatePicker();
            datePickerView.setMaxDate(calendar.getTimeInMillis());
            datePickerDialog.show();
        }


        private void updateLabel(boolean isStartDate){
            String dateFormat = "dd-MMM-yyyy";
            String dateToString;

            if (isStartDate){
                startDate = dateRangeCalendar.getTime();
                dateToString = Utilities.dateToString(startDate, dateFormat);
                if (endDate == null || startDate.compareTo(endDate) < 0){
                    tvStartDate.setText(dateToString);
                } else {
                    if (this.isAdded() && this.getUserVisibleHint()) {
                        Toast.makeText(getActivity(), "Enter Valid date", Toast.LENGTH_SHORT).show();
                    }
                    tvStartDate.setText(dateFormat);
                }
            } else {
                endDate = dateRangeCalendar.getTime();
                dateToString = Utilities.dateToString(endDate, dateFormat);

                if (startDate == null || startDate.compareTo(endDate) < 0){
                    tvEndDate.setText(dateToString);
                } else {
                    if (this.isAdded() && this.getUserVisibleHint()) {
                        Toast.makeText(getActivity(), "Enter Valid date", Toast.LENGTH_SHORT).show();
                    }
                    tvEndDate.setText(dateFormat);
                }
            }
        }

        @OnClick(R.id.imgbtn_apply)
        void saveFilter(){
            if (sortBy == null){
                closeFilter(applied_filters);
                return;
            }
            RealmQuery<GithubItemEntity> realmQuery = githubItemEntities.where();
            if (startDate != null && endDate != null){
                realmQuery = realmQuery.between("createdAt", startDate, endDate);
            }

            if (orderBy != null && orderBy.equalsIgnoreCase("desc")){
                realmQuery = realmQuery.sort(sortBy, Sort.DESCENDING);
            } else {
                realmQuery = realmQuery.sort(sortBy, Sort.ASCENDING);

            }
            githubItemEntities = realmQuery.findAll();
            EventBus.getDefault().post(new FilterUpdateEvent(githubItemEntities));
            closeFilter(applied_filters);

        }

        @OnClick(R.id.imgbtn_refresh)
        void refreshList(){
            EventBus.getDefault().post(new RefreshListEvent());
            closeFilter(applied_filters);

        }
        @OnClick(R.id.img_close_filter)
        void closePopUp(){
            closeFilter(applied_filters);

        }


        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            String sortBy = null, orderBy = null;
            switch (compoundButton.getId()){
                case R.id.star:
                    if (isChecked){
                        sortBy = "stargazers_count";
                    } else {
                        sortBy = null;
                    }
                    break;
                case R.id.fork:
                    if (isChecked){
                        sortBy = "forks_count";
                    } else {
                        sortBy = null;
                    }
                    break;
                case R.id.updated:
                    if (isChecked){
                        sortBy = "updated_at";
                    } else {
                        sortBy = null;
                    }
                    break;
                case R.id.desc:
                    if (isChecked){
                        orderBy = "desc";
                    } else {
                        orderBy = null;
                    }
                    break;
                case R.id.asc:
                    if (isChecked){
                        orderBy = "asc";
                    } else {
                        orderBy = null;
                    }
                    break;
            }
            if (sortBy != null){
                this.sortBy = sortBy;
            }
            if (orderBy != null){
                this.orderBy = orderBy;
            }
        }
    }

    private void showInternetErrorDialog(){
        if(getActivity() == null){
            return;
        }
        new AlertDialog.Builder(getActivity())
                .setMessage(getResources().getString(R.string.internet_error))
                .setPositiveButton("OK", null).show();
    }


    private static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    private void hideKeyboard(Context context, View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


}
