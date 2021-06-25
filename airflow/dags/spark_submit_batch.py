from airflow import DAG

from airflow.contrib.operators.spark_submit_operator import SparkSubmitOperator
from datetime import datetime, timedelta


args = {
    'owner': 'airflow',
    'start_date': datetime(2018, 5, 24)
}
dag = DAG('spark_job', default_args=args, schedule_interval="0 1 * * *")

operator = SparkSubmitOperator(
    task_id='spark_submit_batch',
    application='/usr/local/airflow/app/batch-assembly-0.1.0-SNAPSHOT.jar',
    total_executor_cores='1',
    executor_cores='1',
    executor_memory='2g',
    num_executors='1',
    name='airflow-batch',
    verbose=False,
    driver_memory='1g',
    conf={'master':'spark://spark-master:7077'},
    dag=dag,
)
