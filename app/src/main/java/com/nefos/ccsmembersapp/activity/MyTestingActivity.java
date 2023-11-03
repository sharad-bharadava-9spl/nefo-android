package com.nefos.ccsmembersapp.activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nefos.ccsmembersapp.R;
import com.nefos.ccsmembersapp.adpater.RecyclerViewArrayAdapter;
import com.nefos.ccsmembersapp.databinding.ActivityMyTestingBinding;
import com.nefos.ccsmembersapp.interfaces.OnItemClickListener;
import com.nefos.ccsmembersapp.model.BaseModel;
import com.nefos.ccsmembersapp.model.TempChildModel;
import com.nefos.ccsmembersapp.model.TempParentModel;

import java.util.ArrayList;

public class MyTestingActivity extends BaseActivity implements OnItemClickListener<BaseModel>{

    ArrayList<BaseModel> list;
    ActivityMyTestingBinding binding;
    RecyclerViewArrayAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_testing);

        prepareTempList();
        setRecyclerView();
    }

    void prepareTempList()
    {
          list = new ArrayList<>();

          TempParentModel obj1 = new TempParentModel();
          obj1.title = "Group 1";
          obj1.childList = childList("1");
          list.add(obj1);

        TempParentModel obj2 = new TempParentModel();
        obj2.title = "Group 2";
        obj2.childList = childList("2");
        list.add(obj2);

        TempParentModel obj3 = new TempParentModel();
        obj3.title = "Group 3";
        obj3.childList = childList("3");
        list.add(obj3);

        TempParentModel obj4 = new TempParentModel();
        obj4.title = "Group 4";
        obj4.childList = childList("4");
        list.add(obj4);

        TempParentModel obj5 = new TempParentModel();
        obj5.title = "Group 5";
        obj5.childList = childList("5");
        list.add(obj5);
    }

    ArrayList<TempChildModel> childList(String tag)
    {
        ArrayList<TempChildModel> list = new ArrayList<TempChildModel>();
        for(int i=0; i<5; i++)
        {
            TempChildModel obj = new TempChildModel();
            obj.title = tag+" Child "+(i+1);
            list.add(obj);
        }
        return list;
    }

    @Override
    public void onItemClick(View view, BaseModel object) {
       if(object instanceof TempParentModel)
       {
           TempParentModel parentModel = (TempParentModel) object;
           toast(parentModel.title);
       }else if(object instanceof TempChildModel)
       {
           TempChildModel childModel = (TempChildModel) object;
           toast(childModel.title);
       }

    }

    private void setRecyclerView() {
        adapter = new RecyclerViewArrayAdapter(list, this);
        adapter.setActivity(this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);

        binding.recyclerView.setLayoutManager(mLayoutManager);
        binding.recyclerView.setAdapter(adapter);
    }


}
