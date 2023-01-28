FROM amazoncorretto:17

RUN yum update -y && \
    yum install git -y && \
    yum clean all

RUN git config --global user.email "ci@spliterash.ru" && \
    git config --global user.name "Jenkins"