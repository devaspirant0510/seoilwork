import React, { useEffect, useState } from 'react';
import styled, { keyframes } from 'styled-components';
//npm install styled-components
import { useNavigate } from 'react-router-dom';

const fadeIn = keyframes`
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
`;

const Container = styled.div`
  padding: 2rem;
  background: linear-gradient(to right, #f5f7fa, #c3cfe2);
  min-height: 100vh;
  font-family: 'Segoe UI', sans-serif;
`;

const Title = styled.h2`
  text-align: center;
  color: #333;
  margin-bottom: 2rem;
  font-size: 2.5rem;
`;

const CardGrid = styled.div`
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
`;

const Card = styled.div`
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
  animation: ${fadeIn} 0.6s ease forwards;
  transition: transform 0.3s ease, box-shadow 0.3s ease;

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 6px 20px rgba(0,0,0,0.15);
  }
`;

const Name = styled.h3`
  margin: 0;
  color: #2c3e50;
`;

const Email = styled.p`
  margin: 0.5rem 0;
  color: #555;
`;

const Company = styled.p`
  margin: 0;
  font-style: italic;
  color: #888;
`;

const Loading = styled.p`
  text-align: center;
  font-size: 1.2rem;
  color: #666;
`;

const ErrorMessage = styled.p`
  color: red;
  text-align: center;
  font-weight: bold;
`;

function UsersList() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  const navigate = useNavigate();

  useEffect(() => {
    fetch('https://jsonplaceholder.typicode.com/users')
      .then((res) => {
        if (!res.ok) throw new Error('데이터를 불러오지 못했습니다.');
        return res.json();
      })
      .then((data) => {
        setUsers(data);
        setLoading(false);
      })
      .catch((err) => {
        setError(err.message);
        setLoading(false);
      });
  }, []);

  if (loading) return <Loading>✨ 사용자 데이터를 불러오는 중입니다...</Loading>;
  if (error) return <ErrorMessage>⚠️ 오류 발생: {error}</ErrorMessage>;

  return (
    <Container>
      <Title>👥 사용자 목록</Title>
      <CardGrid>
        {users.map(user => (
          <Card
          key={user.id}
          onClick={() => navigate(`/users/${user.id}`)} 
          style={{ cursor: 'pointer'}}
          >
            <Name>{user.name}</Name>
            <Email>📧 {user.email}</Email>
            <Company>🏢 {user.company.name}</Company>
          </Card>
        ))}
      </CardGrid>
    </Container>
  );
}

export default UsersList;
