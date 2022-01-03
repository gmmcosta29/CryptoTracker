package com.example.cryptocurrencytracker;

import android.app.Activity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.cryptocurrencytracker.ui.FragmentCoinDetails;
import com.example.cryptocurrencytracker.ui.MainFragment;
import com.example.cryptocurrencytracker.ui.ViewModel;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class ChatFragment extends Fragment {

    private FirebaseListAdapter<ChatMessage> adapter;
    private ViewModel vm;
    public static ChatFragment newInstance() {
        return new ChatFragment();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.chat_activity, container, false);
        vm= new ViewModelProvider(requireActivity()).get(ViewModel.class);
        setHasOptionsMenu(true);
        Button fab =
                (Button)v.findViewById(R.id.send_msg_btn);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = (EditText)v.findViewById(R.id.input_chat);

                // Read the input field and push a new instance
                // of ChatMessage to the Firebase database
                FirebaseDatabase.getInstance()
                        .getReference()
                        .push()
                        .setValue(new ChatMessage(input.getText().toString(),
                                FirebaseAuth.getInstance()
                                        .getCurrentUser()
                                        .getDisplayName())
                        );

                // Clear the input
                input.setText("");
            }
        });
        ListView listOfMessages = (ListView)v.findViewById(R.id.list_of_messages);

        //System.out.println("----------> \n My activity!:"+getActivity() + " Instance firebase:"+ FirebaseDatabase.getInstance("https://cryptocurrency-tracker-4ff4b-default-rtdb.europe-west1.firebasedatabase.app"));
        adapter = new FirebaseListAdapter<ChatMessage>(getActivity(), ChatMessage.class,
                R.layout.message, FirebaseDatabase.getInstance("https://cryptocurrency-tracker-4ff4b-default-rtdb.europe-west1.firebasedatabase.app").getReference()) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                // Get references to the views of message.xml
                TextView messageText = (TextView)v.findViewById(R.id.message_text);
                TextView messageUser = (TextView)v.findViewById(R.id.message_user);
                TextView messageTime = (TextView)v.findViewById(R.id.message_time);

                // Set their text
                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());

                // Format the date before showing it
                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                        model.getMessageTime()));
            }
        };

        listOfMessages.setAdapter(adapter);
        return v;

    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.back_btn,menu);
        //MenuItem searchMenuItem = menu.findItem(R.id.search);

        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.back){
            // reset the choice
            vm.setChoice(null);

            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.container, new MainFragment());
            ft.commit();

            return true;
        }
        return false;
    }
/*
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        MenuInflater menuInflater = getActivity().getMenuInflater();
        super.onCreateOptionsMenu(menu, inflater);
    }

 */
}


