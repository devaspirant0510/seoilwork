//기본라이브러리 선언
import React, {useEffect, useState, useSyncExternalStore} from 'react'
function MemberList(){
    //사용할 변수 선언(useState)
    const [members, setMembers]=useState([]);
    const [loading, setLoading]=useState(true);
    const [error, setError]=useState(null);
    //전송받을 데이터처리(useEffect)
    useEffect(()=>{
        fetch('/api/members')
        .then(resp => resp.json())
        .then((data)=>{
            setMembers(data);
            setLoading(false);
        })
        .catch((err)=>{
            setError(err.message);
            setLoading(false);
        })
    },[]);
    
    //상태확인
    if(loading) return <p>로딩 중.....</p>;
    if(error) return <p>오류발생:{error}</p>;
    
    //구현할 페이지를 작성
    return(        
            <ul>
                {members.map((member)=>(
                    <li key={member.username}>
                        {member.username} - {member.password} - {member.email}
                    </li>
                ))}
            </ul>
       );
}

//내보기
export default MemberList;