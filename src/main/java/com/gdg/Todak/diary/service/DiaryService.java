package com.gdg.Todak.diary.service;

import com.gdg.Todak.diary.dto.DiaryRequest;
import com.gdg.Todak.diary.dto.DiaryResponse;
import com.gdg.Todak.diary.dto.EmotionRequest;
import com.gdg.Todak.diary.entity.Diary;
import com.gdg.Todak.diary.repository.DiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;

    @Transactional
    public void writeDiary(DiaryRequest diaryRequest) {
        // 멤버 도메인 완성 후 검증, 이미 금일 작성된 일기가 있는지 확인 로직 추가 예정

        Diary diary = Diary.builder()
                .title(diaryRequest.title())
                .content(diaryRequest.content())
                .emotion(diaryRequest.emotion())
                .build();

        diaryRepository.save(diary);
    }

    @Transactional(readOnly = true)
    public Page<DiaryResponse> readAllDiary(Pageable pageable) {
        return diaryRepository.findAll(pageable)
                .map(Diary -> new DiaryResponse(
                        Diary.getId(),
                        Diary.getTitle(),
                        Diary.getContent(),
                        Diary.getEmotion()
                ));
    }

    @Transactional(readOnly = true)
    public DiaryResponse readDiary(Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new RuntimeException("diary id에 해당하는 일기가 없습니다."));

        return new DiaryResponse(diary.getId(), diary.getTitle(), diary.getContent(), diary.getEmotion());
    }

    @Transactional
    public void updateDiary(Long diaryId, DiaryRequest diaryRequest) {
        //멤버 도메인 추가 후 수정권한 검증 로직 추가 예정 및 글로벌 어드바이스 추가 전 RuntimeException으로 임시 작성
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new RuntimeException("diary id에 해당하는 일기가 없습니다."));

        diary.updateDiary(diaryRequest.title(), diaryRequest.content(), diaryRequest.emotion());
    }

    @Transactional
    public void updateDiaryEmotion(Long diaryId, EmotionRequest emotionRequest) {
        //멤버 도메인 추가 후 수정권한 검증 로직 추가 예정 및 글로벌 어드바이스 추가 전 RuntimeException으로 임시 작성
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new RuntimeException("diary id에 해당하는 일기가 없습니다."));

        diary.setEmotion(emotionRequest.emotion());
    }

    @Transactional
    public void deleteDiary(Long diaryId) {
        //멤버 도메인 추가 후 수정권한 검증 로직 추가 예정 및 글로벌 어드바이스 추가 전 RuntimeException으로 임시 작성
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new RuntimeException("diary id에 해당하는 일기가 없습니다."));

        diaryRepository.delete(diary);
    }
}
