package app.uvsy.service;

import app.uvsy.database.DBConnection;
import app.uvsy.database.DynamoDBDAO;
import app.uvsy.database.exceptions.DBException;
import app.uvsy.model.Correlative;
import app.uvsy.model.CorrelativeCondition;
import app.uvsy.model.CorrelativeRestriction;
import app.uvsy.model.Course;
import app.uvsy.model.Subject;
import app.uvsy.model.reports.subject.SubjectReport;
import app.uvsy.queries.SubjectReportQuery;
import app.uvsy.service.exceptions.RecordActiveException;
import app.uvsy.service.exceptions.RecordNotFoundException;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class SubjectService {


    public Subject getSubject(String subjectId) {
        try (ConnectionSource conn = DBConnection.create()) {
            Dao<Subject, String> subjectsDao = DaoManager.createDao(conn, Subject.class);
            return Optional.ofNullable(subjectsDao.queryForId(subjectId))
                    .orElseThrow(() -> new RecordNotFoundException(subjectId));
        } catch (SQLException | IOException e) {
            // TODO: Add logger error
            e.printStackTrace();
            throw new DBException(e);
        }
    }

    public void updateSubject(String subjectId, String name, String codename, Integer level, Integer hours, Integer points, Boolean optative) {

        try (ConnectionSource conn = DBConnection.create()) {
            Dao<Subject, String> subjectsDao = DaoManager.createDao(conn, Subject.class);
            Subject subject = Optional.ofNullable(subjectsDao.queryForId(subjectId))
                    .orElseThrow(() -> new RecordNotFoundException(subjectId));

            subject.setName(name);
            subject.setCodename(codename);
            subject.setHours(hours);
            subject.setPoints(points);
            subject.setOptative(optative);

            subjectsDao.update(subject);
        } catch (SQLException | IOException e) {
            // TODO: Add logger error
            e.printStackTrace();
            throw new DBException(e);
        }
    }


    public void activateSubject(String subjectId) {
        try (ConnectionSource conn = DBConnection.create()) {
            Dao<Subject, String> subjectsDao = DaoManager.createDao(conn, Subject.class);
            Subject subject = subjectsDao.queryForId(subjectId);
            if (!subject.isActive()) {
                subject.activate();
                subjectsDao.update(subject);
            }
        } catch (SQLException | IOException e) {
            // TODO: Add logger error
            e.printStackTrace();
            throw new DBException(e);
        }
    }

    public void deleteSubject(String subjectId) {
        try (ConnectionSource conn = DBConnection.create()) {
            Dao<Subject, String> subjectsDao = DaoManager.createDao(conn, Subject.class);
            Subject subject = subjectsDao.queryForId(subjectId);

            if (subject.isActive()) {
                throw new RecordActiveException(subjectId);
            }

            subjectsDao.delete(subject);
        } catch (SQLException | IOException e) {
            // TODO: Add logger error
            e.printStackTrace();
            throw new DBException(e);
        }
    }

    public Object getCorrelatives(String subjectId) {
        try (ConnectionSource conn = DBConnection.create()) {
            Dao<Subject, String> subjectDao = DaoManager.createDao(conn, Subject.class);

            boolean subjectExists = Optional.ofNullable(subjectDao.queryForId(subjectId)).isPresent();

            if (subjectExists) {
                Dao<Correlative, String> correlativeDao = DaoManager.createDao(conn, Correlative.class);
                return correlativeDao.queryBuilder()
                        .selectColumns()
                        .where()
                        .eq("subject_id", subjectId)
                        .query();
            }
            throw new RecordNotFoundException(subjectId);
        } catch (SQLException | IOException e) {
            // TODO: Add logger error
            e.printStackTrace();
            throw new DBException(e);
        }
    }

    public void createCorrelative(String subjectId, String correlativeSubjectId, CorrelativeRestriction restriction, CorrelativeCondition condition) {
        try (ConnectionSource conn = DBConnection.create()) {
            Dao<Subject, String> subjectDao = DaoManager.createDao(conn, Subject.class);

            boolean subjectExists = Optional.ofNullable(subjectDao.queryForId(subjectId)).isPresent();

            if (subjectExists) {

                Correlative correlative = new Correlative();
                correlative.setSubjectId(subjectId);
                correlative.setCorrelativeSubjectId(correlativeSubjectId);
                correlative.setCorrelativeCondition(condition);
                correlative.setCorrelativeRestriction(restriction);

                Dao<Correlative, String> correlativeDao = DaoManager.createDao(conn, Correlative.class);
                correlativeDao.create(correlative);
            } else {
                throw new RecordNotFoundException(subjectId);
            }
        } catch (SQLException | IOException e) {
            // TODO: Add logger error
            e.printStackTrace();
            throw new DBException(e);
        }
    }

    public List<Course> getCourses(String subjectId) {
        Course course = new Course();
        course.setSubjectId(subjectId);

        DynamoDBDAO<Course> courseDAO = DynamoDBDAO.createFor(Course.class);
        return courseDAO.query(course, "SubjectIdIndex");
    }

    public void createCourse(String subjectId, String commissionId) {
        Course course = new Course();
        course.setCommissionId(commissionId);
        course.setSubjectId(subjectId);
        course.setActive(Boolean.TRUE);

        DynamoDBDAO<Course> courseDAO = DynamoDBDAO.createFor(Course.class);
        courseDAO.save(course);
    }

    public SubjectReport getReport(String subjectId) {
        return new SubjectReportQuery(subjectId).execute();
    }
}
