package app.uvsy.model;

import app.uvsy.model.course.CoursingPeriod;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGenerateStrategy;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBGeneratedUuid;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Optional;

@ToString
@Getter
@Setter
@DynamoDBTable(tableName = "courses")
public class Course {

    @DynamoDBHashKey(attributeName = "course_id")
    @DynamoDBIndexRangeKey(globalSecondaryIndexNames = {"SubjectIdIndex", "CommissionIdIndex"}, attributeName = "course_id")
    @DynamoDBGeneratedUuid(value = DynamoDBAutoGenerateStrategy.CREATE)
    private String courseId;

    @DynamoDBAttribute(attributeName = "commission_id")
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "CommissionIdIndex", attributeName = "commission_id")
    private String commissionId;

    @DynamoDBAttribute(attributeName = "subject_id")
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "SubjectIdIndex", attributeName = "subject_id")
    private String subjectId;

    @DynamoDBAttribute(attributeName = "periods")
    private List<CoursingPeriod> periods;

    @DynamoDBAttribute(attributeName = "active")
    private Boolean active;

    @DynamoDBIgnore
    public boolean isActive() {
        return Optional.ofNullable(active).orElse(Boolean.FALSE);
    }

    @DynamoDBIgnore
    public void activate() {
        this.active = Boolean.TRUE;
    }


}
