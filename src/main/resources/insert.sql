DECLARE @json VARCHAR(MAX) = ?

INSERT INTO CSTM_CPY.dbo.T_ArsHitLog(DateTimeFr, DateTimeTo, CallKey, HostName, MenuId)
SELECT DateTimeFr, DateTimeTo, CallKey, HostName, MenuId
FROM OPENJSON(@json) WITH(
    DateTimeFr DATETIME 'strict $.datetimefr',
    DateTimeTo DATETIME 'strict $.datetimeto',
    CallKey VARCHAR(20) 'strict $.callkey',
    HostName VARCHAR(20) 'strict $.hostname',
    MenuId VARCHAR(10) 'strict $.menuid'
)