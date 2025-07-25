import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import styled from 'styled-components';

const Wrapper = styled.div`
  padding: 2rem;
  font-family: 'Segoe UI', sans-serif;
`;

const BackLink = styled(Link)`
  display: inline-block;
  margin-bottom: 1rem;
  color: #3498db;
  text-decoration: none;

  &:hover {
    text-decoration: underline;
  }
`;

const DetailCard = styled.div`
  background: #fff;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
`;

function UserDetail() {
  const { id } = useParams();
  const [user, setUser] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetch(`https://jsonplaceholder.typicode.com/users/${id}`)
      .then(res => {
        if (!res.ok) throw new Error('사용자 정보를 불러올 수 없습니다.');
        return res.json();
      })
      .then(data => setUser(data))
      .catch(err => setError(err.message));
  }, [id]);

  if (error) return <Wrapper>⚠️ 오류: {error}</Wrapper>;
  if (!user) return <Wrapper>⏳ 사용자 정보 로딩 중...</Wrapper>;

  return (
    <Wrapper>
      <BackLink to="/">← 사용자 목록으로</BackLink>
      <DetailCard>
        <h2>{user.name} 님의 상세 정보</h2>
        <p><strong>이메일:</strong> {user.email}</p>
        <p><strong>전화번호:</strong> {user.phone}</p>
        <p><strong>웹사이트:</strong> {user.website}</p>
        <p><strong>회사:</strong> {user.company.name}</p>
        <p><strong>주소:</strong> {user.address.city}, {user.address.street}</p>
      </DetailCard>
    </Wrapper>
  );
}

export default UserDetail;
