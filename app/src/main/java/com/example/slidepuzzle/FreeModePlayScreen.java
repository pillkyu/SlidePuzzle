package com.example.slidepuzzle;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FreeModePlayScreen extends AppCompatActivity {
    private GridLayout puzzleGrid;
    private List<Integer> puzzlePieces;
    private List<Integer> puzzlereturn;
    private List<Bitmap> pieces;
    private int moveCount = 0;

    private String selectedImageUriString;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_mode_play_screen);
        puzzleGrid = findViewById(R.id.free_puzzle);
        Intent intent = getIntent();
        if (intent != null) {
            selectedImageUriString = intent.getStringExtra("selected_image");
            if (selectedImageUriString != null) {
                Uri selectedImageUri = Uri.parse(selectedImageUriString);

                try {
                    Bitmap tempimage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);

                Resources resources = getResources();
                int screenWidth = resources.getDisplayMetrics().widthPixels;
                Bitmap fullImage = Bitmap.createScaledBitmap(tempimage, screenWidth, screenWidth, true);

                // 이미지뷰에 Uri 설정하여 이미지 출력
                /*SquareImageView imageView = findViewById(R.id.free_mode_play_placeholder);
                imageView.setImageBitmap(fullImage);*/
                int num = intent.getIntExtra("selected_num", 3);

                initializePuzzle(fullImage, num);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        }


        ImageButton btnHint = findViewById(R.id.btn_hint);
        FragmentContainerView fragmentContainerView = findViewById(R.id.fragment_container);

        //hint버튼 눌렷을 때의 동작 구현
        btnHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Fragment표시
                fragmentContainerView.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HintFragment())
                        .commit();
            }
        });

        //홈버튼 구현
        ImageButton btnHome = findViewById(R.id.btn_home);
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //BtnHome클래스의 goHome 메서드 호출
                BtnHome.goHome(FreeModePlayScreen.this);
            }
        });
    }
        private void initializePuzzle(Bitmap fullImage,int num) {

            if (puzzleGrid == null) {
                Log.e("FreeModePlayScreen", "puzzleGrid is null");
                return; // puzzleGrid가 초기화되지 않았다면 메서드를 종료
            }

            Log.d("FreeModePlayScreen", "Initializing puzzle");

                pieces = cutImageIntoPieces(fullImage, num, num);
                puzzlePieces = new ArrayList<>();
                for (int i = 0; i < num*num; i++) {//여기서 퍼즐사이즈가 정해짐 여기에 num 추가하면 퍼즐사이즈가 정해짐
                    puzzlePieces.add(i);
                }

                //getInversionCount 사용해서 셔플한 퍼즐피스 검증하기
                puzzlePieces = getsufflePuzzle(puzzlePieces);

                for (int i = 0; i < num*num; i++) {

                    int temp = i;

                    ImageView imageView = new ImageView(this);
                    int width = fullImage.getWidth() / num;
                    int height = fullImage.getHeight() / num;
                    if (puzzlePieces.get(i) == num*num-1) {
                        imageView.setImageBitmap(createEmptyBitmap(width, height));
                    } else {
                        imageView.setImageBitmap(pieces.get(puzzlePieces.get(i)));
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                int clickedPiece = puzzlePieces.get(temp);
                                if (isValidMove(clickedPiece,num)) {
                                    swapPieces(clickedPiece,num);
                                    updatePuzzleView(width, height,num);


                                    if (isPuzzleComplete()) {
                                        Toast.makeText(FreeModePlayScreen.this, "퍼즐이 완성되었습니다!", Toast.LENGTH_SHORT).show();
                                        Intent outintent = new Intent(FreeModePlayScreen.this, FreeModeClearpageScreen.class);
                                        outintent.putExtra("move_count", moveCount);
                                        outintent.putExtra("selecet_image",selectedImageUriString);

                                        // 다음 액티비티 시작
                                        startActivity(outintent);
                                    }
                                }
                            }
                        });
                    }
                    puzzleGrid.addView(imageView);
                }
            }
    private boolean isValidMove(int clickedPiece, int num) {//num을 입력받게 매개변수 추가필요
        int emptyIndex = puzzlePieces.indexOf(num*num-1);
        int clickedIndex = puzzlePieces.indexOf(clickedPiece);

        int emptyRow = emptyIndex / num;
        int emptyCol = emptyIndex % num;
        int clickedRow = clickedIndex / num;
        int clickedCol = clickedIndex % num;


        return (Math.abs(emptyRow - clickedRow) == 1 && emptyCol == clickedCol) ||
                (Math.abs(emptyCol - clickedCol) == 1 && emptyRow == clickedRow);
    }

    private void swapPieces(int clickedPiece, int num) {//num을 입력받게 매개변수 추가필요
        int emptyIndex = puzzlePieces.indexOf(num*num-1);
        int clickedIndex = puzzlePieces.indexOf(clickedPiece);

        Collections.swap(puzzlePieces, emptyIndex, clickedIndex);

        moveCount++;
    }


    private void updatePuzzleView(int width,int height, int num) {//num을 입력받게 매개변수 추가필요
        puzzleGrid.removeAllViews();
        for (int i = 0; i < num*num; i++) {
            int row = i / num;
            int col = i % num;
            int temp= i;
            ImageView imageView = new ImageView(this);
            if (puzzlePieces.get(i) == num*num-1) {
                imageView.setImageBitmap(createEmptyBitmap(width, height));
            }else {
                imageView.setImageBitmap(pieces.get(puzzlePieces.get(i)));
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int clickedPiece = puzzlePieces.get(temp);
                        if (isValidMove(clickedPiece,num)) {
                            swapPieces(clickedPiece,num);
                            updatePuzzleView(width,height,num);

                          /*  if (isPuzzleComplete()) {

                                Toast.makeText(FreeModePlayScreen.this, "퍼즐이 완성되었습니다!", Toast.LENGTH_SHORT).show();
                            }*/
                        }
                    }
                });
            }
            puzzleGrid.addView(imageView);
        }
    }

    private List<Bitmap> cutImageIntoPieces(Bitmap fullImage, int rows, int cols) {
        List<Bitmap> pieces = new ArrayList<>();

        int pieceWidth = fullImage.getWidth() / cols;
        int pieceHeight = fullImage.getHeight() / rows;

        int squareSize = Math.min(pieceWidth, pieceHeight);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int x = j * pieceWidth;
                int y = i * pieceHeight;
                Bitmap piece = Bitmap.createBitmap(fullImage, x, y, pieceWidth, pieceHeight);
                pieces.add(piece);
            }
        }

        return pieces;
    }

    private Bitmap createEmptyBitmap(int width, int height) {
        Bitmap emptyBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        emptyBitmap.eraseColor(android.graphics.Color.TRANSPARENT);
        return emptyBitmap;
    }

    private boolean isPuzzleComplete() {
        for (int i = 0; i < puzzlePieces.size(); i++) {
            if (puzzlePieces.get(i) != i) {
                return false;
            }
        }
        return true;
    }

    private Integer getInversionCount(List<Integer> puzzlePieces){
        int inversionCount=0;
        int length=puzzlePieces.size();

        for(int i=0;  i<length-1; i++){
            for(int j=i+1; j<length; j++){
                if(puzzlePieces.get(i) > puzzlePieces.get(j) && puzzlePieces.get(i)!=length-1 && puzzlePieces.get(j)!=length-1){
                    inversionCount++;
                }
            }
        }
        return inversionCount;
    }
    private List<Integer> getsufflePuzzle(List<Integer> puzzlePieces){
        puzzlereturn=new ArrayList<>();

        while(true) {
            Collections.shuffle(puzzlePieces);
            if (puzzlePieces.size() == 9 || puzzlePieces.size() == 25){
                if(getInversionCount(puzzlePieces)%2==0){
                    break;
                }
            }
            if(puzzlePieces.size() == 16){
                if(getInversionCount(puzzlePieces)%2==0){
                    if(puzzlePieces.indexOf(15)/4==1||puzzlePieces.indexOf(15)/4==3){
                        break;
                    }
                }
                if(getInversionCount(puzzlePieces)%2==1){
                    if(puzzlePieces.indexOf(15)/4==0||puzzlePieces.indexOf(15)/4==2){
                        break;
                    }
                }
            }
        }
        puzzlereturn=puzzlePieces;
        return puzzlereturn;
    }

}








