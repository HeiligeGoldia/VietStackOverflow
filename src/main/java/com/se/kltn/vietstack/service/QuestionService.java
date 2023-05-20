package com.se.kltn.vietstack.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.se.kltn.vietstack.model.question.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class QuestionService {

    Firestore db = FirestoreClient.getFirestore();

    //    ---------- Question ----------

    public String getLastQid() throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("Question");
        ApiFuture<QuerySnapshot> api = ref.get();
        QuerySnapshot doc = api.get();
        List<QueryDocumentSnapshot> docs = doc.getDocuments();
        if(docs.size() == 0){
            return "0";
        }
        else{
            List<Integer> docId = new ArrayList<>();
            for(QueryDocumentSnapshot ds : docs){
                docId.add(Integer.parseInt(ds.getId()));
            }
            Collections.sort(docId);
            return String.valueOf(docId.get(docId.size()-1));
        }
    }

    public String create(Question question) throws ExecutionException, InterruptedException {
        int newQid = Integer.parseInt(getLastQid()) + 1;
        String qid = String.valueOf(newQid);
        question.setQid(qid);

        ApiFuture<WriteResult> api = db.collection("Question").document(question.getQid()).set(question);
        api.get();
        return question.getQid();
    }

    public String edit(Question question) throws ExecutionException, InterruptedException {
        ApiFuture<WriteResult> api = db.collection("Question").document(question.getQid()).set(question);
        api.get();
        return question.getQid();
    }

    public List<Integer> getSearchQuestionTitle(String input) throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("Question");
        Query query = ref.whereNotEqualTo("qid", "0");
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();

        List<Integer> docId = new ArrayList<>();
        for(QueryDocumentSnapshot ds : docs) {
            Question qs = ds.toObject(Question.class);
            if(qs.getTitle().contains(input)){
                docId.add(Integer.parseInt(qs.getQid()));
            }
        }
        return docId;
    }

    public List<Integer> getSearchQuestionDetail(String input) throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("QuestionDetail");
        Query query = ref.whereNotEqualTo("qdid", "0");
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();

        List<Integer> docId = new ArrayList<>();
        for(QueryDocumentSnapshot ds : docs) {
            QuestionDetail qd = ds.toObject(QuestionDetail.class);
            if(qd.getContent().contains(input)){
                if(!docId.contains(qd.getQid())){
                    docId.add(Integer.parseInt(qd.getQid()));
//                    System.out.println(qd);
                }
            }
        }
        return docId;
    }

    public List<Question> getAllQuestionList() throws ExecutionException, InterruptedException {
        List<Question> ql = new ArrayList<>();
        List<Integer> docId = new ArrayList<>();
        CollectionReference ref = db.collection("Question");
        Query query = ref.whereNotEqualTo("qid", "0");
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        if(docs.isEmpty()){
            return ql;
        }
        else {
            for (QueryDocumentSnapshot ds : docs) {
                docId.add(Integer.parseInt(ds.getId()));
            }
            Collections.sort(docId, Collections.reverseOrder());
            for(Integer i : docId) {
                ql.add(ref.document(String.valueOf(i)).get().get().toObject(Question.class));
            }
            return ql;
        }
    }

    public Question getQuestionByQid(String qid) throws ExecutionException, InterruptedException {
        Question question;
        DocumentReference ref = db.collection("Question").document(qid);
        ApiFuture<DocumentSnapshot> api = ref.get();
        DocumentSnapshot doc = api.get();
        if(doc.exists()){
            question = doc.toObject(Question.class);
            return question;
        }
        return new Question();
    }

    public String closeQuestion(String qid, Boolean aa) throws ExecutionException, InterruptedException {
        Question q = getQuestionByQid(qid);
        if(q.getStatus().equals("Open")){
            if(aa){
                q.setStatus("Closed");
            }
            else {
                return "Can not close question without any answer accepted";
            }
        }
        else if(q.getStatus().equals("Closed")) {
            q.setStatus("Open");
        }
        ApiFuture<WriteResult> api = db.collection("Question").document(q.getQid()).set(q);
        api.get();
        return "Modified";
    }

    public List<Question> getQidByUid(String uid) throws ExecutionException, InterruptedException {
        List<Question> qtl = new ArrayList<>();
        List<Integer> docId = new ArrayList<>();
        CollectionReference ref = db.collection("Question");
        Query query = ref.whereEqualTo("uid", uid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        if(docs.isEmpty()){
            return qtl;
        }
        else {
            for (QueryDocumentSnapshot d : docs) {
                docId.add(Integer.parseInt(d.getId()));
            }
            Collections.sort(docId, Collections.reverseOrder());

            for(Integer i : docId) {
                qtl.add(ref.document(String.valueOf(i)).get().get().toObject(Question.class));
            }
            return qtl;
        }
    }

    public String delete(String qid) {
        try {
            ApiFuture<WriteResult> writeResult = db.collection("Question").document(qid).delete();
            writeResult.get();
            return "Question deleted";
        } catch (InterruptedException e) {
            return "Question not found";
        } catch (ExecutionException e) {
            return "Question not found";
        }
    }

    public List<Question> getQidByTid(String tid) throws ExecutionException, InterruptedException {
        List<Question> qtl = new ArrayList<>();
        List<Integer> docId = new ArrayList<>();
        CollectionReference ref = db.collection("QuestionTag");
        Query query = ref.whereEqualTo("tid", tid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        if(docs.isEmpty()){
            return qtl;
        }
        else {
            for (QueryDocumentSnapshot d : docs) {
                QuestionTag tag = d.toObject(QuestionTag.class);
                docId.add(Integer.parseInt(tag.getQid()));
            }
            Collections.sort(docId, Collections.reverseOrder());

            for(Integer i : docId) {
                qtl.add(getQuestionByQid(String.valueOf(i)));
            }
            return qtl;
        }
    }

    public int getSlQuestionTotal() throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("Question");
        Query query = ref.whereNotEqualTo("qid", "0");
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        return docs.size();
    }

    public int getSlQuestionInMonthYear(int month, int year) throws ExecutionException, InterruptedException {
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date dateBegin = calendar.getTime();

        int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.DAY_OF_MONTH, lastDay);
        Date dateEnd = calendar.getTime();

        CollectionReference ref = db.collection("Question");
        Query query = ref.whereNotEqualTo("qid", "0")
                .whereGreaterThanOrEqualTo("date", dateBegin)
                .whereLessThanOrEqualTo("date", dateEnd);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        return docs.size();
    }

//    public HashMap getSlQuestionInYear(int year) throws ExecutionException, InterruptedException {
//        HashMap qiy = new HashMap();
//        for(int i = 1; i < 12; i++) {
//            getSlQuestionInMonthYear()
//        }
//    }

    //    ---------- Question Tag ----------

    public String getLastTqid() throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("QuestionTag");
        ApiFuture<QuerySnapshot> api = ref.get();
        QuerySnapshot doc = api.get();
        List<QueryDocumentSnapshot> docs = doc.getDocuments();
        if(docs.size() == 0){
            return "0";
        }
        else{
            List<Integer> docId = new ArrayList<>();
            for(QueryDocumentSnapshot ds : docs){
                docId.add(Integer.parseInt(ds.getId()));
            }
            Collections.sort(docId);
            return String.valueOf(docId.get(docId.size()-1));
        }
    }

    public String addTagToPost(QuestionTag tag) throws ExecutionException, InterruptedException {
        int newTqid = Integer.parseInt(getLastTqid()) + 1;
        String tqid = String.valueOf(newTqid);
        tag.setTqid(tqid);

        ApiFuture<WriteResult> api = db.collection("QuestionTag").document(tag.getTqid()).set(tag);
        api.get();
        return tag.getTqid();
    }

    public List<QuestionTag> getQuestionTagByQid(String qid) throws ExecutionException, InterruptedException {
        List<QuestionTag> qtl = new ArrayList<>();
        CollectionReference ref = db.collection("QuestionTag");
        Query query = ref.whereEqualTo("qid", qid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        if(docs.isEmpty()){
            return qtl;
        }
        else {
            for (QueryDocumentSnapshot d : docs) {
                qtl.add(d.toObject(QuestionTag.class));
            }
            return qtl;
        }
    }

    public QuestionTag getQuestionTagByQidTid(String qid, String tid) throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("QuestionTag");
        Query query = ref.whereEqualTo("qid", qid).whereEqualTo("tid", tid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        for(QueryDocumentSnapshot ds : docs){
            return ds.toObject(QuestionTag.class);
        }
        return new QuestionTag();
    }

    public String removeQuestionTag(QuestionTag questionTag) {
        try {
            ApiFuture<WriteResult> writeResult = db.collection("QuestionTag").document(questionTag.getTqid()).delete();
            writeResult.get();
            return "Tag removed";
        } catch (ExecutionException e) {
            return "Tag not found";
        } catch (InterruptedException e) {
            return "Tag not found";
        }
    }

    public String removeTagsByQid(String qid) throws ExecutionException, InterruptedException {
        List<QuestionTag> qtl = getQuestionTagByQid(qid);
        if(qtl.isEmpty()) {
            return "Question tag null";
        }
        else {
            for(QuestionTag qt : qtl) {
                removeQuestionTag(qt);
            }
            return "All question tag removed";
        }
    }

    //    ---------- Question Detail ----------

    public String getLastQdid() throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("QuestionDetail");
        ApiFuture<QuerySnapshot> api = ref.get();
        QuerySnapshot doc = api.get();
        List<QueryDocumentSnapshot> docs = doc.getDocuments();
        if(docs.size() == 0){
            return "0";
        }
        else{
            List<Integer> docId = new ArrayList<>();
            for(QueryDocumentSnapshot ds : docs){
                docId.add(Integer.parseInt(ds.getId()));
            }
            Collections.sort(docId);
            return String.valueOf(docId.get(docId.size()-1));
        }
    }

    public String createDetail(QuestionDetail questionDetail) throws ExecutionException, InterruptedException {
        int newQdid = Integer.parseInt(getLastQdid()) + 1;
        String qdid = String.valueOf(newQdid);
        questionDetail.setQdid(qdid);

        ApiFuture<WriteResult> api = db.collection("QuestionDetail").document(questionDetail.getQdid()).set(questionDetail);
        api.get();
        return questionDetail.getQdid();
    }

    public List<QuestionDetail> getQuestionDetailByQid(String qid) throws ExecutionException, InterruptedException {
        List<QuestionDetail> qdl = new ArrayList<>();
        List<Integer> docId = new ArrayList<>();
        CollectionReference ref = db.collection("QuestionDetail");
        Query query = ref.whereEqualTo("qid", qid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        if(docs.isEmpty()){
            return qdl;
        }
        else {
            for (QueryDocumentSnapshot ds : docs) {
                docId.add(Integer.parseInt(ds.getId()));
            }
            Collections.sort(docId);

            for(Integer i : docId) {
                qdl.add(ref.document(String.valueOf(i)).get().get().toObject(QuestionDetail.class));
            }
            return qdl;
        }
    }

    public String removeAllDetailByQid(String qid) throws ExecutionException, InterruptedException {
        List<QuestionDetail> qdl = getQuestionDetailByQid(qid);
        if(qdl.isEmpty()) {
            return "Details list empty!";
        }
        else {
            for(QuestionDetail qd : qdl) {
                try {
                    ApiFuture<WriteResult> writeResult = db.collection("QuestionDetail").document(qd.getQdid()).delete();
                    writeResult.get();
                } catch (ExecutionException e) {
                    return "Detail not found";
                } catch (InterruptedException e) {
                    return "Detail not found";
                }
            }
            return "Details deleted";
        }
    }

    //    ---------- Question Vote ----------

    public String getLastVqid() throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("QuestionVote");
        ApiFuture<QuerySnapshot> api = ref.get();
        QuerySnapshot doc = api.get();
        List<QueryDocumentSnapshot> docs = doc.getDocuments();
        if(docs.size() == 0){
            return "0";
        }
        else{
            List<Integer> docId = new ArrayList<>();
            for(QueryDocumentSnapshot ds : docs){
                docId.add(Integer.parseInt(ds.getId()));
            }
            Collections.sort(docId);
            return String.valueOf(docId.get(docId.size()-1));
        }
    }

    public QuestionVote getQuestionVoteByUidQid(String uid, String qid) throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("QuestionVote");
        Query query = ref.whereEqualTo("uid", uid).whereEqualTo("qid", qid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        for(QueryDocumentSnapshot ds : docs){
            return ds.toObject(QuestionVote.class);
        }
        return new QuestionVote();
    }

    public List<QuestionVote> getQuestionVoteByQid(String qid) throws ExecutionException, InterruptedException {
        List<QuestionVote> ids = new ArrayList<>();
        CollectionReference ref = db.collection("QuestionVote");
        Query query = ref.whereEqualTo("qid", qid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        for(QueryDocumentSnapshot ds : docs){
            ids.add(ds.toObject(QuestionVote.class));
        }
        return ids;
    }

    public String removeQuestionVote(QuestionVote questionVote) {
        try{
            ApiFuture<WriteResult> writeResult = db.collection("QuestionVote").document(questionVote.getVqid()).delete();
            writeResult.get();
            return "Vote removed";
        } catch (ExecutionException e) {
            return "Vote not found";
        } catch (InterruptedException e) {
            return "Vote not found";
        }
    }

    public String removeQuestionVoteByQid(String qid) throws ExecutionException, InterruptedException {
        List<QuestionVote> qvl = getQuestionVoteByQid(qid);
        if(qvl.isEmpty()) {
            return "Question vote null";
        }
        else {
            for(QuestionVote qv : qvl) {
                removeQuestionVote(qv);
            }
            return "All question vote removed";
        }
    }

    public String castQuestionVote(QuestionVote questionVote) throws ExecutionException, InterruptedException {
        QuestionVote qv = getQuestionVoteByUidQid(questionVote.getUid(), questionVote.getQid());
        if(qv.getVqid()==null){
            int newVqid = Integer.parseInt(getLastVqid()) + 1;
            String vqid = String.valueOf(newVqid);
            questionVote.setVqid(vqid);
            ApiFuture<WriteResult> api = db.collection("QuestionVote").document(questionVote.getVqid()).set(questionVote);
            api.get();
            return questionVote.getVqid();
        }
        else {
            if(questionVote.getValue().equals(qv.getValue())){
                return removeQuestionVote(qv);
            }
            else {
                qv.setValue(questionVote.getValue());
                ApiFuture<WriteResult> api = db.collection("QuestionVote").document(qv.getVqid()).set(qv);
                api.get();
                return qv.getVqid();
            }
        }
    }

    public int getTotalVoteValue(String qid) throws ExecutionException, InterruptedException {
        int totalValue = 0;
        CollectionReference ref = db.collection("QuestionVote");
        Query query = ref.whereEqualTo("qid", qid).whereIn("value", Arrays.asList("Up", "Down"));
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        for(QueryDocumentSnapshot ds : docs){
            if(ds.toObject(QuestionVote.class).getValue().equals("Up")){
                totalValue++;
            }
            else if(ds.toObject(QuestionVote.class).getValue().equals("Down")){
                totalValue--;
            }
        }
        return totalValue;
    }

//    public int getTotalVoteCount(String qid) throws ExecutionException, InterruptedException {
//        int count = 0;
//        CollectionReference ref = db.collection("QuestionVote");
//        Query query = ref.whereEqualTo("qid", qid).whereIn("value", Arrays.asList("Up", "Down"));
//        ApiFuture<QuerySnapshot> querySnapshot = query.get();
//        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
//        for(QueryDocumentSnapshot ds : docs){
//            count++;
//        }
//        return count;
//    }

    public String getUserVoteValue(String uid, String qid) throws ExecutionException, InterruptedException {
        QuestionVote qv = getQuestionVoteByUidQid(uid, qid);
        if(qv.getVqid()==null) {
            return "0";
        }
        else {
            return qv.getValue();
        }
    }

    //    ---------- Question Activity History ----------

    public String getLastQahid() throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("QuestionActivityHistory");
        ApiFuture<QuerySnapshot> api = ref.get();
        QuerySnapshot doc = api.get();
        List<QueryDocumentSnapshot> docs = doc.getDocuments();
        if(docs.size() == 0){
            return "0";
        }
        else{
            List<Integer> docId = new ArrayList<>();
            for(QueryDocumentSnapshot ds : docs){
                docId.add(Integer.parseInt(ds.getId()));
            }
            Collections.sort(docId);
            return String.valueOf(docId.get(docId.size()-1));
        }
    }

    public String createActivityHistory(QuestionActivityHistory questionActivityHistory) throws ExecutionException, InterruptedException {
        int newQahid = Integer.parseInt(getLastQahid()) + 1;
        String qahid = String.valueOf(newQahid);
        questionActivityHistory.setQahid(qahid);

        ApiFuture<WriteResult> api = db.collection("QuestionActivityHistory").document(questionActivityHistory.getQahid()).set(questionActivityHistory);
        api.get();
        return questionActivityHistory.getQahid();
    }

    public List<QuestionActivityHistory> getQuestionActivityHistory(String qid) throws ExecutionException, InterruptedException {
        List<QuestionActivityHistory> qal = new ArrayList<>();
        List<Integer> docId = new ArrayList<>();
        CollectionReference ref = db.collection("QuestionActivityHistory");
        Query query = ref.whereEqualTo("qid", qid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        if(docs.isEmpty()){
            return qal;
        }
        else {
            for (QueryDocumentSnapshot ds : docs) {
                docId.add(Integer.parseInt(ds.getId()));
            }
            Collections.sort(docId);

            for(Integer i : docId) {
                qal.add(ref.document(String.valueOf(i)).get().get().toObject(QuestionActivityHistory.class));
            }
            return qal;
        }
    }

    public String deleteHistoryByQahid(String qahid) {
        try{
            ApiFuture<WriteResult> writeResult = db.collection("QuestionActivityHistory").document(qahid).delete();
            writeResult.get();
            return "Activity history deleted";
        } catch (ExecutionException e) {
            return "Activity history not found";
        } catch (InterruptedException e) {
            return "Activity history not found";
        }
    }

    public String deleteHistoryByQid(String qid) throws ExecutionException, InterruptedException {
        List<QuestionActivityHistory> qal = getQuestionActivityHistory(qid);
        if(qal.isEmpty()) {
            return "Question history null";
        }
        else {
            for(QuestionActivityHistory qa : qal) {
                deleteHistoryByQahid(qa.getQahid());
            }
            return "All history deleted";
        }
    }

    //    ---------- Question Report ----------

    public String getLastRqid() throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("QuestionReport");
        ApiFuture<QuerySnapshot> api = ref.get();
        QuerySnapshot doc = api.get();
        List<QueryDocumentSnapshot> docs = doc.getDocuments();
        if(docs.size() == 0){
            return "0";
        }
        else{
            List<Integer> docId = new ArrayList<>();
            for(QueryDocumentSnapshot ds : docs){
                docId.add(Integer.parseInt(ds.getId()));
            }
            Collections.sort(docId);
            return String.valueOf(docId.get(docId.size()-1));
        }
    }

    public QuestionReport getReportByRqid(String rqid) throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("QuestionReport");
        Query query = ref.whereEqualTo("rqid", rqid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        for(QueryDocumentSnapshot ds : docs){
            return ds.toObject(QuestionReport.class);
        }
        return new QuestionReport();
    }

    public QuestionReport getReportByUidQid(String uid, String qid) throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("QuestionReport");
        Query query = ref.whereEqualTo("uid", uid).whereEqualTo("qid", qid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        for(QueryDocumentSnapshot ds : docs){
            return ds.toObject(QuestionReport.class);
        }
        return new QuestionReport();
    }

    public String report(QuestionReport questionReport) throws ExecutionException, InterruptedException {
        QuestionReport qr = getReportByUidQid(questionReport.getUid(), questionReport.getQid());
        if(qr.getRqid()==null){
            int newRqid = Integer.parseInt(getLastRqid()) + 1;
            String rqid = String.valueOf(newRqid);
            questionReport.setRqid(rqid);

            ApiFuture<WriteResult> api = db.collection("QuestionReport").document(questionReport.getRqid()).set(questionReport);
            api.get();
            return questionReport.getRqid();
        }
        else {
            return "Already reported";
        }
    }

    public List<QuestionReport> getQuestionReport() throws ExecutionException, InterruptedException {
        List<QuestionReport> qrl = new ArrayList<>();
        List<Integer> docId = new ArrayList<>();
        CollectionReference ref = db.collection("QuestionReport");
        Query query = ref.whereNotEqualTo("rqid", "0");
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        if(docs.isEmpty()){
            return qrl;
        }
        else {
            for(QueryDocumentSnapshot ds : docs) {
                docId.add(Integer.parseInt(ds.getId()));
            }
            Collections.sort(docId);
            for(Integer i : docId) {
                qrl.add(ref.document(String.valueOf(i)).get().get().toObject(QuestionReport.class));
            }
            return qrl;
        }
    }

    public List<QuestionReport> getUserReport(String uid) throws ExecutionException, InterruptedException {
        List<QuestionReport> qrl = new ArrayList<>();
        CollectionReference ref = db.collection("QuestionReport");
        Query query = ref.whereEqualTo("uid", uid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();

        List<Integer> docId = new ArrayList<>();
        for(QueryDocumentSnapshot ds : docs) {
            docId.add(Integer.parseInt(ds.getId()));
        }
        Collections.sort(docId);

        for(Integer i : docId) {
            qrl.add(ref.document(String.valueOf(i)).get().get().toObject(QuestionReport.class));
        }
        return qrl;
    }

    public List<QuestionReport> getAllQuestionReportByQid(String qid) throws ExecutionException, InterruptedException {
        List<QuestionReport> qrl = new ArrayList<>();
        CollectionReference ref = db.collection("QuestionReport");
        Query query = ref.whereEqualTo("qid", qid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();

        List<Integer> docId = new ArrayList<>();
        for(QueryDocumentSnapshot ds : docs) {
            docId.add(Integer.parseInt(ds.getId()));
        }
        Collections.sort(docId, Collections.reverseOrder());

        for(Integer i : docId) {
            qrl.add(ref.document(String.valueOf(i)).get().get().toObject(QuestionReport.class));
        }
        return qrl;
    }

    public String editReport(QuestionReport questionReport) throws ExecutionException, InterruptedException {
        ApiFuture<WriteResult> api = db.collection("QuestionReport").document(questionReport.getRqid()).set(questionReport);
        api.get();
        return questionReport.getRqid();
    }

    public String deleteReport(String rqid){
        try{
            ApiFuture<WriteResult> writeResult = db.collection("QuestionReport").document(rqid).delete();
            writeResult.get();
            return "Report deleted";
        } catch (ExecutionException e) {
            return "Report not found";
        } catch (InterruptedException e) {
            return "Report not found";
        }
    }

}
