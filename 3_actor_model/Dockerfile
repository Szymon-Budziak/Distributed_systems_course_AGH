FROM python:3.10

ARG WORKDIR
WORKDIR ${WORKDIR}

COPY requirements.txt ${WORKDIR}
COPY ray-lab-1-task.py ${WORKDIR}
COPY ray-lab-2-objects.py ${WORKDIR}
COPY ray-lab-3-actors.py ${WORKDIR}
COPY data/* ${WORKDIR}/data/

RUN python3 -m venv venv
RUN . venv/bin/activate
RUN pip install -r requirements.txt

CMD ["sh", "-c", "python3 ray-lab-1-task.py && python3 ray-lab-2-objects.py && ray start --head && python3 ray-lab-3-actors.py"]